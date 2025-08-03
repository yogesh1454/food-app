-- Create permissions table
CREATE TABLE permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    resource VARCHAR(100) NOT NULL,
    action VARCHAR(50) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create role_permissions table
CREATE TABLE role_permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    role VARCHAR(50) NOT NULL,
    permission_id UUID NOT NULL REFERENCES permissions(id),
    is_active BOOLEAN NOT NULL DEFAULT true,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(role, permission_id)
);

-- Create indexes for better performance
CREATE INDEX idx_permissions_resource_action ON permissions(resource, action);
CREATE INDEX idx_permissions_is_active ON permissions(is_active);
CREATE INDEX idx_role_permissions_role ON role_permissions(role);
CREATE INDEX idx_role_permissions_is_active ON role_permissions(is_active);
CREATE INDEX idx_role_permissions_permission_id ON role_permissions(permission_id);

-- Insert default permissions
INSERT INTO permissions (name, description, resource, action) VALUES
-- Admin permissions
('manage_users', 'Manage all users', 'users', 'manage'),
('manage_vendors', 'Manage all vendors', 'vendors', 'manage'),
('manage_delivery_partners', 'Manage delivery partners', 'delivery_partners', 'manage'),
('view_reports', 'View all reports', 'reports', 'view'),
('manage_system', 'Manage system settings', 'system', 'manage'),

-- Vendor permissions
('manage_own_profile', 'Manage own profile', 'profile', 'manage'),
('manage_menu', 'Manage menu items', 'menu', 'manage'),
('manage_orders', 'Manage orders', 'orders', 'manage'),
('view_own_reports', 'View own reports', 'reports', 'view_own'),

-- Delivery partner permissions
('manage_deliveries', 'Manage deliveries', 'deliveries', 'manage'),
('update_location', 'Update location', 'location', 'update'),

-- Customer permissions
('place_orders', 'Place orders', 'orders', 'place'),
('view_order_history', 'View order history', 'orders', 'view_history');

-- Assign permissions to roles
INSERT INTO role_permissions (role, permission_id) 
SELECT 'ADMIN', id FROM permissions WHERE name IN (
    'manage_users', 'manage_vendors', 'manage_delivery_partners', 
    'view_reports', 'manage_system', 'manage_own_profile'
);

INSERT INTO role_permissions (role, permission_id) 
SELECT 'VENDOR', id FROM permissions WHERE name IN (
    'manage_own_profile', 'manage_menu', 'manage_orders', 'view_own_reports'
);

INSERT INTO role_permissions (role, permission_id) 
SELECT 'DELIVERY_PARTNER', id FROM permissions WHERE name IN (
    'manage_own_profile', 'manage_deliveries', 'update_location'
);

INSERT INTO role_permissions (role, permission_id) 
SELECT 'CUSTOMER', id FROM permissions WHERE name IN (
    'manage_own_profile', 'place_orders', 'view_order_history'
); 