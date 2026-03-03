-- Override partner API keys with {noop} for test verification
INSERT INTO partners (id, partner_key, api_key_hash, created_at)
VALUES (gen_random_uuid(), 'partner-a', '{noop}partner-a-secret', CURRENT_TIMESTAMP)
ON CONFLICT (partner_key) DO UPDATE SET api_key_hash = '{noop}partner-a-secret';

INSERT INTO partners (id, partner_key, api_key_hash, created_at)
VALUES (gen_random_uuid(), 'partner-b', '{noop}partner-b-secret', CURRENT_TIMESTAMP)
ON CONFLICT (partner_key) DO UPDATE SET api_key_hash = '{noop}partner-b-secret';
