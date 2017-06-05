DROP TRIGGER IF EXISTS t_updroundplayers ON roundplayers;
DROP FUNCTION IF EXISTS updRoundPlayers();

CREATE FUNCTION updRoundPlayers() RETURNS TRIGGER AS
$$
DECLARE
  numplayers integer;
BEGIN

  IF TG_OP = 'DELETE' THEN

      UPDATE rounds AS r
        SET estado = 'F', -- Se marca partida finalizada si abandona un jugador 
            numberofplayers = numberofplayers - 1
      WHERE r.roundid = OLD.roundid;

      RETURN OLD;

  ELSIF TG_OP = 'INSERT' THEN
      IF EXISTS(SELECT r.roundid 
                FROM rounds AS r
                WHERE r.roundid = NEW.roundid AND 
                      r.estado <> 'A' AND r.estado <> 'O' )
      THEN
          RETURN NULL; --Si la partida ya esta marcada como finalizada (F) o cerrada (C) 
      END IF;

      SELECT getNextPlayer(NEW.roundid) INTO numplayers;

      RAISE NOTICE 'Numero de jugadores = %', numplayers;

      IF numplayers = -1 THEN
          RETURN NULL; --Si superamos el numero max de jugadores
      END IF;

      UPDATE rounds AS r
        SET numberofplayers = numplayers
      WHERE r.roundid = NEW.roundid;

      UPDATE rounds AS r
        SET estado = 
            CASE 
                WHEN g.maxplayers =  r.numberofplayers THEN 'C' -- Closed
                WHEN g.minplayers <= r.numberofplayers THEN 'A' -- Active
                WHEN g.minplayers >  r.numberofplayers THEN 'O' -- Open
                ELSE 'E' -- Error. No deberia llegar aqui
            END
      FROM games AS g
      WHERE r.roundid = NEW.roundid AND r.gameid=g.gameid;
  END IF;

  RETURN NEW;

END
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER t_updroundplayers BEFORE INSERT OR DELETE ON roundplayers 
                               FOR EACH ROW EXECUTE PROCEDURE updRoundPlayers();


