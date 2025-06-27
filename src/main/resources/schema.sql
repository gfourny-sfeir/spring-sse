-- Create product table
CREATE TABLE IF NOT EXISTS produit (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ean CHAR(13) NOT NULL,
    nom VARCHAR(255) NOT NULL,
    quantite INTEGER NOT NULL DEFAULT 0,
    prix DECIMAL(10, 2) NOT NULL
);

-- Create index on id column
CREATE INDEX IF NOT EXISTS idx_produit_id ON produit(id);
CREATE INDEX IF NOT EXISTS idx_produit_ean ON produit(ean);

-- Create function for notification
CREATE OR REPLACE FUNCTION notify_quantite_change()
RETURNS TRIGGER AS '
BEGIN
    -- Notify on the channel "produit_quantite_change" with the product EAN and new quantity
    PERFORM pg_notify(''produit_quantite_change'', json_build_object(
        ''id'',  NEW.id,
        ''ean'', NEW.ean,
        ''nom'', NEW.nom,
        ''quantite'', NEW.quantite
    )::text);
    RETURN NEW;
END;
' LANGUAGE plpgsql;

-- Create triggers to call the function when quantity changes
-- Trigger for INSERT operations
DROP TRIGGER IF EXISTS produit_quantite_insert_trigger ON produit;
CREATE TRIGGER produit_quantite_insert_trigger
AFTER INSERT ON produit
FOR EACH ROW
EXECUTE FUNCTION notify_quantite_change();

-- Trigger for UPDATE operations
DROP TRIGGER IF EXISTS produit_quantite_update_trigger ON produit;
CREATE TRIGGER produit_quantite_update_trigger
AFTER UPDATE OF quantite ON produit
FOR EACH ROW
WHEN (NEW.quantite <> OLD.quantite)
EXECUTE FUNCTION notify_quantite_change();
