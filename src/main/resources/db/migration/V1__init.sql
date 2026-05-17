-- ============================================================
-- V1__init.sql - Criação das tabelas e dados iniciais
-- Sistema de Cartões de Crédito para Postos de Gasolina
-- ============================================================

CREATE TABLE IF NOT EXISTS customers (
    id            BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    name          VARCHAR(100)        NOT NULL,
    email         VARCHAR(150)        NOT NULL UNIQUE,
    phone         VARCHAR(20),
    cpf           VARCHAR(14)         NOT NULL UNIQUE,
    created_at    DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    customer_status VARCHAR(30)         NOT NULL DEFAULT 'ACTIVE'
);

CREATE TABLE IF NOT EXISTS credit_cards (
    id              BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    card_number     VARCHAR(19)         NOT NULL UNIQUE COMMENT 'Número mascarado (ex: **** **** **** 1234)',
    card_holder     VARCHAR(100)        NOT NULL,
    expiration_date DATE                NOT NULL,
    card_type       VARCHAR(30)         NOT NULL COMMENT 'GAS_STATION | STANDARD_CREDIT | DEBIT',
    credit_limit    DECIMAL(10,2)       NOT NULL DEFAULT 0.00,
    customer_id     BIGINT              NOT NULL,
    CONSTRAINT fk_card_customer FOREIGN KEY (customer_id) REFERENCES customers(id)
);

-- ============================================================
-- Clientes
-- ============================================================
INSERT INTO customers (name, email, phone, cpf, customer_status) VALUES
('Carlos Eduardo Silva',       'carlos.silva@email.com',    '(11) 98765-4321', '123.456.789-00', 'ACTIVE'),
('Ana Paula Ferreira',         'ana.ferreira@email.com',    '(21) 99123-5678', '234.567.890-11', 'ACTIVE'),
('Roberto Alves Mendes',       'roberto.mendes@email.com',  '(31) 97654-3210', '345.678.901-22', 'ACTIVE'),
('Juliana Costa Ramos',        'juliana.ramos@email.com',   '(41) 98001-2345', '456.789.012-33', 'ACTIVE'),
('Marcos Vinicius Oliveira',   'marcos.oliveira@email.com', '(51) 96543-9876', '567.890.123-44', 'ACTIVE'),
('Fernanda Lima Torres',       'fernanda.torres@email.com', '(71) 99876-5432', '678.901.234-55', 'ACTIVE'),
('André Luiz Barbosa',         'andre.barbosa@email.com',   '(85) 98234-6780', '789.012.345-66', 'ACTIVE'),
('Patrícia Souza Nunes',       'patricia.nunes@email.com',  '(62) 97321-5432', '890.123.456-77', 'ACTIVE'),
('Ricardo Pereira Gomes',      'ricardo.gomes@email.com',   '(48) 96789-0123', '901.234.567-88', 'ACTIVE'),
('Luciana Martins Cardoso',    'luciana.cardoso@email.com', '(67) 98456-7890', '012.345.678-99', 'ACTIVE'),
('Evandro Barroso Gaio',       'evandro.gaio@email.com',    '(37) 99123-5066', '456.123.979-00', 'ACTIVE'),
('Angelita Simoes',            'angelita.simoes@email.com', '(11) 99424-4276', '789.321.646-00', 'ACTIVE'),
('Elaine Gaio',                'elaine.gaio@email.com',     '(11) 99398-9273', '456.123.789-00', 'ACTIVE');


-- ============================================================
-- Cartões de Crédito (GAS_STATION — exclusivos para postos)
-- ============================================================
INSERT INTO credit_cards (card_number, card_holder, expiration_date, card_type, credit_limit, customer_id) VALUES
('**** **** **** 1001', 'CARLOS E SILVA',       '2027-05-31', 'GAS_STATION', 3500.00,  1),
('**** **** **** 2002', 'ANA P FERREIRA',       '2026-08-31', 'GAS_STATION', 2800.00,  2),
('**** **** **** 3003', 'ROBERTO A MENDES',     '2028-01-31', 'GAS_STATION', 5000.00,  3),
('**** **** **** 4004', 'JULIANA C RAMOS',      '2027-11-30', 'GAS_STATION', 4200.00,  4),
('**** **** **** 5005', 'MARCOS V OLIVEIRA',    '2026-03-31', 'GAS_STATION', 1500.00,  5),
('**** **** **** 6006', 'FERNANDA L TORRES',    '2028-07-31', 'GAS_STATION', 6000.00,  6),
('**** **** **** 7007', 'ANDRE L BARBOSA',      '2027-09-30', 'GAS_STATION', 2200.00,  7),
('**** **** **** 8008', 'PATRICIA S NUNES',     '2026-12-31', 'GAS_STATION', 3800.00,  8),
('**** **** **** 9009', 'RICARDO P GOMES',      '2028-04-30', 'GAS_STATION', 4500.00,  9),
('**** **** **** 0010', 'LUCIANA M CARDOSO',    '2027-06-30', 'GAS_STATION', 7000.00, 10)
;

-- Alguns cartões extras de outros tipos (para dar contexto de filtro)
INSERT INTO credit_cards (card_number, card_holder, expiration_date, card_type, credit_limit, customer_id) VALUES
('**** **** **** 1111', 'CARLOS E SILVA',       '2026-10-31', 'STANDARD_CREDIT', 8000.00,     1),
('**** **** **** 2222', 'ANA P FERREIRA',       '2027-02-28', 'DEBIT',           0.00,        2),
('**** **** **** 3333', 'ROBERTO A MENDES',     '2028-06-30', 'STANDARD_CREDIT', 12000.00,    3),
('**** **** **** 1234', 'Evandro Barroso Gaio', '2028-05-28', 'DEBIT',           33333330.00, 11),
('**** **** **** 3456', 'Angelita Simoes',      '2028-06-30', 'DEBIT',           300000.00,   12),
('**** **** **** 5678', 'Elaine Gaio',          '2028-06-30', 'DEBIT',           332420.00,   13);

