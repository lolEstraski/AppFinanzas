INSERT INTO payment_methods (name, type, is_default, user_id, created_at, updated_at) VALUES
    ('Efectivo', 'CASH', TRUE, NULL, now(), now()),
    ('Tarjeta', 'CARD', TRUE, NULL, now(), now());
