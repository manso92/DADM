DROP TRIGGER IF EXISTS t_updroundmoves ON roundmoves;
DROP FUNCTION IF EXISTS updRoundMoves();

CREATE FUNCTION updRoundMoves() RETURNS TRIGGER AS
$$
DECLARE
  numplayers integer;
BEGIN

      RAISE NOTICE '%', NEW.roundid;

      IF EXISTS(SELECT r.roundid 
                FROM rounds AS r
                WHERE r.roundid = NEW.roundid AND 
                      r.estado <> 'A' AND r.estado <> 'C' AND lastmovenumber>-1)
      THEN
          RETURN NULL; --Si la partida ya esta marcada como finalizada (F) o cerrada (C) 
      END IF;

      UPDATE rounds AS r
        SET  estado = CASE
                WHEN r.lastmovenumber < 0 THEN r.estado
                ELSE 'C'
             END,
--             turn = 1+((lastmovenumber+1)%numberofplayers),
             turn = 1+(turn%numberofplayers),
             lastmovenumber = lastmovenumber+1,
             lastchange = now()
        WHERE r.roundid = NEW.roundid;

  RETURN NEW;

END
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER t_updroundmoves BEFORE INSERT ON roundmoves 
                                 FOR EACH ROW EXECUTE PROCEDURE updRoundMoves();


