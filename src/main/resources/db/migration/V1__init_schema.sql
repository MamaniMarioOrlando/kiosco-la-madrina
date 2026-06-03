CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    role VARCHAR(50) NOT NULL,
    avatar_url TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    barcode VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    price NUMERIC(38,2) NOT NULL,
    stock_quantity INTEGER NOT NULL,
    category_id BIGINT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES categories (id)
);

CREATE TABLE IF NOT EXISTS sales (
    id BIGSERIAL PRIMARY KEY,
    date_time TIMESTAMP NOT NULL,
    total_amount NUMERIC(38,2) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    user_id BIGINT,
    CONSTRAINT fk_sale_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS sale_details (
    id BIGSERIAL PRIMARY KEY,
    sale_id BIGINT,
    product_id BIGINT,
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(38,2) NOT NULL,
    subtotal NUMERIC(38,2) NOT NULL,
    CONSTRAINT fk_sdetail_sale FOREIGN KEY (sale_id) REFERENCES sales (id),
    CONSTRAINT fk_sdetail_product FOREIGN KEY (product_id) REFERENCES products (id)
);
