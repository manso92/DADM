-- Devuelve las siguientes propiedades de las partida de un usuario:
--             - roundid
--             - numero de jugadores
--             - fecha ultima actualizacion
--             - nicks de los jugadores
--             - ultimo estado del tablero
--             - estado del tablero
--
CREATE OR REPLACE FUNCTION getUserRounds(gameid integer,playerid uuid) 
            RETURNS TABLE(roundid integer, 
                          numberofplayers integer,
                          dateevent timestamp with time zone,
                          playernames text,
                          turn integer,
                          codedboard character varying(2048),
                          estado "char") AS
$$
DECLARE

BEGIN

    RETURN QUERY (SELECT rrpp.roundid,
                         rrpp.numberofplayers,
                         rrpp.datecreated,
                         rrpp.pnames,
                         rrpp.turn,
                         rm.codedboard,
                         rrpp.estado 
                  FROM  roundmoves AS rm,
                        (SELECT r.roundid, r.numberofplayers,r.datecreated,
                                string_agg(p.playername, ',' ORDER BY rp.turn) AS pnames,
                                r.turn, r.estado, r.lastmovenumber
                         FROM   rounds AS r, 
                                roundplayers AS rp, 
                                players AS p
                         WHERE  r.roundid IN (SELECT rrr.roundid
                                              FROM   rounds AS rrr, 
                                                     roundplayers AS rp2
                                              WHERE rrr.roundid=rp2.roundid AND
                                                    rp2.playerid=$2
                                              GROUP BY rrr.roundid) AND
                                r.roundid=rp.roundid AND 
                                rp.playerid=p.playerid AND
                                r.gameid = $1
                         GROUP BY r.roundid ) AS rrpp
                  WHERE rrpp.roundid = rm.roundid AND 
                        rm.nummove=rrpp.lastmovenumber);

END;
$$ LANGUAGE 'plpgsql';

