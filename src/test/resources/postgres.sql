CREATE TABLE CLIENT
(
    ID   bigserial UNIQUE NOT NULL,
    NAME text
);

CREATE TABLE ACCOUNT
(
    ID        bigserial UNIQUE NOT NULL,
    AMOUNT    decimal,
    NUMBER    text,
    CLIENT_ID bigint REFERENCES CLIENT (ID)
);