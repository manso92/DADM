CREATE OR REPLACE FUNCTION getOpenRounds(gameid integer) 
            RETURNS TABLE(roundid integer, 
                          numberofplayers integer,
                          dateevent timestamp with time zone,
                          playernames text,
                          turn integer,
                          codedboard character varying(2048)) AS
$$
DECLARE

BEGIN

    RETURN QUERY (SELECT rrpp.roundid,
                         rrpp.numberofplayers,
                         rrpp.datecreated,
                         rrpp.pnames,
                         rrpp.turn,
                         rm.codedboard
                  FROM  roundmoves AS rm, 
                        (SELECT r.roundid, r.numberofplayers,r.datecreated,
                                r.turn,
                                string_agg(p.playername, ',' ORDER BY rp.turn) AS pnames
                        FROM    rounds AS r, 
                                roundplayers AS rp, 
                                players AS p  
                        WHERE   r.roundid=rp.roundid AND 
                                rp.playerid=p.playerid AND
                                r.gameid = $1 AND 
                                (r.estado='O' OR r.estado='A')
                        GROUP BY r.roundid) AS rrpp
                  WHERE rrpp.roundid = rm.roundid);

END;
$$ LANGUAGE 'plpgsql';

