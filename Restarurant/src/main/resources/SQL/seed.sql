-- ============================================================
--  HOTEL BOOKING SYSTEM — PREMIUM SEED DATA
--  Inspired by luxury hotels like Four Seasons Mumbai
-- ============================================================

-- ─────────────────────────────────────────────
--  ROLES
-- ─────────────────────────────────────────────
INSERT INTO roles (name) VALUES
    ('ROLE_ADMIN'),
    ('ROLE_STAFF'),
    ('ROLE_CUSTOMER');

-- ─────────────────────────────────────────────
--  USERS
-- ─────────────────────────────────────────────
INSERT INTO users (full_name, email, password, phone, role_id) VALUES

-- ADMIN
(
    'Hotel Admin',
    'admin@hotel.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    -- admin123
    '9000000001',
    (SELECT id FROM roles WHERE name = 'ROLE_ADMIN')
),

-- STAFF
(
    'Reception Staff',
    'staff@hotel.com',
    '$2a$10$ByIUiNaRfBKSV6urOrx6dOQsGYKKoiS.TT3/.WKZM3Y1QZQ7pjim6',
    -- staff123
    '9000000002',
    (SELECT id FROM roles WHERE name = 'ROLE_STAFF')
),

-- CUSTOMERS
(
    'Mana Dhanak',
    'mana@email.com',
    '$2a$10$8K1p/a0dR1xqM2LtFj6oiebBe3WWDQ.qyZ3vYzDO.8rZ.3yHoJHKi',
    -- mana123
    '9876543210',
    (SELECT id FROM roles WHERE name = 'ROLE_CUSTOMER')
),

(
    'Fiona Williams',
    'fiona@email.com',
    '$2a$10$Gt6DOl4f5kGJcSf9rZ1j7OFYIEn.Wm5KvI3dZl3/9LRQx3lP2ZuKi',
    -- fiona123
    '9876543211',
    (SELECT id FROM roles WHERE name = 'ROLE_CUSTOMER')
);

-- ─────────────────────────────────────────────
--  ROOMS
-- ─────────────────────────────────────────────
INSERT INTO rooms
(room_number, type, price_per_night, capacity, description, image_url, is_available)
VALUES

-- SINGLE ROOMS
(
    '101',
    'SINGLE',
    6500.00,
    1,
    'Luxury single room with elegant interiors, smart lighting, high-speed Wi-Fi, marble bathroom, and stunning Mumbai skyline views.',
    'https://images.unsplash.com/photo-1566073771259-6a8506099945',
    TRUE
),

(
    '102',
    'SINGLE',
    6800.00,
    1,
    'Modern business-class single room featuring a queen-size bed, work desk, coffee machine, and rain shower bathroom.',
    'https://images.unsplash.com/photo-1522708323590-d24dbb6b0267',
    TRUE
),

(
    '103',
    'SINGLE',
    7000.00,
    1,
    'Premium single room with sea-facing windows, wooden flooring, mood lighting, and luxury bath amenities.',
    'https://images.unsplash.com/photo-1505693416388-ac5ce068fe85',
    TRUE
),

-- DOUBLE ROOMS
(
    '201',
    'DOUBLE',
    11500.00,
    2,
    'Elegant double room with two queen beds, smart TV, lounge seating, minibar, and floor-to-ceiling windows.',
    'https://images.unsplash.com/photo-1590490360182-c33d57733427',
    TRUE
),

(
    '202',
    'DOUBLE',
    12500.00,
    2,
    'Luxury double room with private balcony overlooking the Arabian Sea and access to the executive lounge.',
    'https://images.unsplash.com/photo-1578683010236-d716f9a3f461',
    TRUE
),

(
    '203',
    'DOUBLE',
    13200.00,
    2,
    'Spacious premium double room featuring contemporary interiors, marble bathroom, bathtub, and mood-controlled lighting.',
    'https://images.unsplash.com/photo-1551882547-ff40c63fe5fa',
    TRUE
),

-- DELUXE ROOMS
(
    '301',
    'DELUXE',
    18500.00,
    3,
    'Deluxe luxury suite with panoramic city skyline views, jacuzzi, king-size bed, private lounge, and complimentary breakfast buffet.',
    'https://images.unsplash.com/photo-1542314831-068cd1dbfeeb',
    TRUE
),

(
    '302',
    'DELUXE',
    19500.00,
    3,
    'Ocean-view deluxe room with premium interiors, walk-in wardrobe, workspace, minibar, and personalized concierge service.',
    'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b',
    TRUE
),

(
    '303',
    'DELUXE',
    21000.00,
    3,
    'Signature deluxe room inspired by Four Seasons-style interiors featuring luxury Italian furniture and smart automation.',
    'https://images.unsplash.com/photo-1566665797739-1674de7a421a',
    TRUE
),

-- SUITES
(
    '401',
    'SUITE',
    32000.00,
    4,
    'Presidential Suite featuring two bedrooms, private dining space, luxury bathtub, skyline terrace, and dedicated butler service.',
    'https://images.unsplash.com/photo-1578898887932-dce23a595ad4',
    TRUE
),

(
    '402',
    'SUITE',
    35000.00,
    4,
    'Ultra-luxury suite with private plunge pool, spa-style bathroom, entertainment lounge, and personalized 24/7 concierge.',
    'https://images.unsplash.com/photo-1445019980597-93fa8acb246c',
    TRUE
),

(
    '403',
    'SUITE',
    45000.00,
    5,
    'Royal Signature Suite inspired by Four Seasons Mumbai with grand interiors, sea-view terrace, dining hall, bar counter, and luxury hospitality services.',
    'https://images.unsplash.com/photo-1455587734955-081b22074882',
    TRUE
);