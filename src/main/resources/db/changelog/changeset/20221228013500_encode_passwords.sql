CREATE extension if not exists pgcrypto;
UPDATE usr SET password = crypt(password, gen_salt('bf', 8));