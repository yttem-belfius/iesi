-- Adding CREATED_BY, CREATED_AT COLUMN in DES_SCRIPT

ALTER TABLE DES_SCRIPT_VRS
ADD COLUMN CREATED_BY VARCHAR(255),
ADD COLUMN CREATED_AT VARCHAR(255);