-- Devuelve las rondas activas para un usuario. Devuelve:
--             - roundid
--             - numero de jugadores
--             - fecha ultima actualizacion
--             - nicks de los jugadores
--             - ultimo estado del tablero
--
CREATE OR REPLACE FUNCTION getActiveRounds(gameid integer,playerid uuid) 
            RETURNS TABLE(roundid integer, 
                          numberofplayers integer,
                          dateevent timestamp with time zone,
                          playernames text,
                          turn integer,
                          codedboard character varying(2048)) AS
$$
DECLARE

BEGIN

    RETURN QUERY (SELECT uurr.roundid,
                         uurr.numberofplayers,
                         uurr.dateevent,
                         uurr.playernames,
                         uurr.turn,
                         uurr.codedboard 
                  FROM  (SELECT * FROM getUserRounds($1,$2)) AS uurr
                  WHERE (uurr.estado='A' OR uurr.estado='C'));

END;
$$ LANGUAGE 'plpgsql';

