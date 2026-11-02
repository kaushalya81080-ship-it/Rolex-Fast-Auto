-- ==============================================================================
-- ROLEX FAST AUTO - SUPABASE BACKEND SCHEMA & ROW LEVEL SECURITY (RLS) POLICIES
-- Target Platform: Supabase PostgreSQL (PostgREST / Supabase Auth)
-- Target Game: Miniclip Carrom Pool: Disc Game ONLY
-- ==============================================================================

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. PROFILES TABLE (Linked to supabase auth.users)
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    email TEXT,
    display_name TEXT,
    auth_provider TEXT DEFAULT 'google',
    is_admin BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 2. ADMIN ROLES
CREATE TABLE IF NOT EXISTS public.admin_roles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE,
    role_name TEXT NOT NULL DEFAULT 'admin',
    granted_at TIMESTAMPTZ DEFAULT NOW(),
    granted_by TEXT DEFAULT 'super_admin'
);

-- 3. ADMIN LICENSES (Separate key system, setup cost ₹500)
CREATE TABLE IF NOT EXISTS public.admin_licenses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    license_key TEXT UNIQUE NOT NULL,
    setup_cost_inr INTEGER NOT NULL DEFAULT 500,
    duration_days INTEGER NOT NULL DEFAULT 365,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    activated_at TIMESTAMPTZ,
    expires_at TIMESTAMPTZ,
    status TEXT NOT NULL DEFAULT 'UNACTIVATED', -- UNACTIVATED, ACTIVE, EXPIRED, REVOKED
    admin_user_id UUID REFERENCES public.profiles(id),
    is_revoked BOOLEAN DEFAULT FALSE
);

-- 4. AUTOPLAY LICENSES (1D, 2D, 3D, 7D, 30D, Custom)
CREATE TABLE IF NOT EXISTS public.autoplay_licenses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    license_key TEXT UNIQUE NOT NULL,
    duration_days INTEGER NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    activated_at TIMESTAMPTZ,
    expires_at TIMESTAMPTZ,
    status TEXT NOT NULL DEFAULT 'UNACTIVATED',
    customer_id UUID REFERENCES public.profiles(id),
    customer_note TEXT,
    device_fingerprint TEXT,
    is_revoked BOOLEAN DEFAULT FALSE
);

-- 5. PAYMENT TRANSACTIONS
CREATE TABLE IF NOT EXISTS public.payment_transactions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    customer_id UUID REFERENCES public.profiles(id),
    customer_name TEXT NOT NULL,
    plan_id TEXT NOT NULL,
    plan_name TEXT NOT NULL,
    amount_inr INTEGER NOT NULL,
    payment_method TEXT NOT NULL DEFAULT 'UPI',
    utr_number TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'PENDING', -- PENDING, VERIFIED, REJECTED
    created_at TIMESTAMPTZ DEFAULT NOW(),
    verified_at TIMESTAMPTZ,
    verified_by UUID REFERENCES public.profiles(id),
    issued_license_key TEXT
);

-- 6. LICENSE ACTIVATIONS (Device binding and session audit)
CREATE TABLE IF NOT EXISTS public.license_activations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    license_key TEXT NOT NULL,
    license_type TEXT NOT NULL, -- AUTOPLAY or ADMIN_PANEL
    activated_by UUID REFERENCES public.profiles(id),
    device_id TEXT NOT NULL,
    activated_at TIMESTAMPTZ DEFAULT NOW(),
    ip_address TEXT
);

-- 7. APPLICATION SETTINGS & PRICING
CREATE TABLE IF NOT EXISTS public.application_settings (
    key TEXT PRIMARY KEY,
    value TEXT NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    updated_by UUID REFERENCES public.profiles(id)
);

-- 8. AUDIT LOGS
CREATE TABLE IF NOT EXISTS public.audit_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    timestamp TIMESTAMPTZ DEFAULT NOW(),
    actor TEXT NOT NULL,
    action TEXT NOT NULL,
    details TEXT
);

-- ==============================================================================
-- ROW LEVEL SECURITY (RLS) POLICIES
-- ==============================================================================

ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.admin_roles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.admin_licenses ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.autoplay_licenses ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.payment_transactions ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.license_activations ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.application_settings ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.audit_logs ENABLE ROW LEVEL SECURITY;

-- Helper function: Is user an authorized administrator?
CREATE OR REPLACE FUNCTION public.is_admin()
RETURNS BOOLEAN AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1 FROM public.admin_roles
        WHERE user_id = auth.uid()
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Profiles: Users can view & update their own profile; admins can view all
CREATE POLICY "Users can view own profile" ON public.profiles
    FOR SELECT USING (auth.uid() = id OR public.is_admin());

CREATE POLICY "Users can update own profile" ON public.profiles
    FOR UPDATE USING (auth.uid() = id);

-- Admin Licenses: Only authenticated admins can view and manage admin licenses
CREATE POLICY "Admins manage admin licenses" ON public.admin_licenses
    FOR ALL USING (public.is_admin());

-- AutoPlay Licenses:
-- Customers can view their own activated license; Admins can view/generate/revoke all
CREATE POLICY "Customers view own autoplay license" ON public.autoplay_licenses
    FOR SELECT USING (customer_id = auth.uid() OR public.is_admin());

CREATE POLICY "Admins full access to autoplay licenses" ON public.autoplay_licenses
    FOR ALL USING (public.is_admin());

-- Payment Transactions:
-- Customers can insert payments and view their own transactions; Admins can verify/update
CREATE POLICY "Customers insert payments" ON public.payment_transactions
    FOR INSERT WITH CHECK (auth.uid() = customer_id);

CREATE POLICY "Customers view own payments" ON public.payment_transactions
    FOR SELECT USING (auth.uid() = customer_id OR public.is_admin());

CREATE POLICY "Admins verify and update payments" ON public.payment_transactions
    FOR UPDATE USING (public.is_admin());

-- Application Settings: Public read, Admin write
CREATE POLICY "Public read app settings" ON public.application_settings
    FOR SELECT USING (true);

CREATE POLICY "Admins update app settings" ON public.application_settings
    FOR ALL USING (public.is_admin());

-- Audit Logs: Admin read/insert only
CREATE POLICY "Admins read audit logs" ON public.audit_logs
    FOR SELECT USING (public.is_admin());

CREATE POLICY "System insert audit logs" ON public.audit_logs
    FOR INSERT WITH CHECK (true);

-- ==============================================================================
-- BACKEND STORED PROCEDURES (Secure key generation & verification)
-- ==============================================================================

-- Stored procedure to verify payment and issue key atomically
CREATE OR REPLACE FUNCTION public.verify_payment_and_issue_key(
    target_payment_id UUID,
    should_approve BOOLEAN,
    admin_id UUID
)
RETURNS JSON AS $$
DECLARE
    target_tx RECORD;
    new_key TEXT;
    duration INT;
BEGIN
    IF NOT public.is_admin() THEN
        RAISE EXCEPTION 'Unauthorized: Only administrators can verify payments.';
    END IF;

    SELECT * INTO target_tx FROM public.payment_transactions WHERE id = target_payment_id;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Transaction not found';
    END IF;

    IF should_approve THEN
        IF target_tx.plan_id = 'admin_license' THEN
            new_key := 'RFAX-ADM-' || UPPER(SUBSTRING(MD5(RANDOM()::TEXT), 1, 4)) || '-' || UPPER(SUBSTRING(MD5(RANDOM()::TEXT), 5, 4));
            INSERT INTO public.admin_licenses(license_key, setup_cost_inr, duration_days, status)
            VALUES (new_key, target_tx.amount_inr, 365, 'UNACTIVATED');
        ELSE
            duration := CASE target_tx.plan_id
                WHEN 'plan_1d' THEN 1
                WHEN 'plan_2d' THEN 2
                WHEN 'plan_3d' THEN 3
                WHEN 'plan_7d' THEN 7
                WHEN 'plan_30d' THEN 30
                ELSE 1
            END;
            new_key := 'RFAX-PLAY-' || UPPER(SUBSTRING(MD5(RANDOM()::TEXT), 1, 4)) || '-' || UPPER(SUBSTRING(MD5(RANDOM()::TEXT), 5, 4));
            INSERT INTO public.autoplay_licenses(license_key, duration_days, status, customer_id, customer_note)
            VALUES (new_key, duration, 'UNACTIVATED', target_tx.customer_id, target_tx.customer_name);
        END IF;

        UPDATE public.payment_transactions
        SET status = 'VERIFIED',
            verified_at = NOW(),
            verified_by = admin_id,
            issued_license_key = new_key
        WHERE id = target_payment_id;

        INSERT INTO public.audit_logs(actor, action, details)
        VALUES (admin_id::TEXT, 'VERIFY_PAYMENT', 'Verified payment ' || target_payment_id || ' and generated key ' || new_key);

        RETURN json_build_object('success', true, 'status', 'VERIFIED', 'issued_key', new_key);
    ELSE
        UPDATE public.payment_transactions
        SET status = 'REJECTED',
            verified_at = NOW(),
            verified_by = admin_id
        WHERE id = target_payment_id;

        RETURN json_build_object('success', true, 'status', 'REJECTED');
    END IF;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
