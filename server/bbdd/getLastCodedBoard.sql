CREATE OR REPLACE FUNCTION getLastCodedBoard(round_id integer) 
            RETURNS TABLE(roundid integer, 
                          nummove integer,
                          codedboard character varying(2048),
                          playerid uuid,
                          movedate timestamp with time zone) AS
$$
DECLARE

BEGIN

    RETURN QUERY (SELECT rm.roundid, rm.nummove, rm.codedboard, rm.playerid, rm.movedate 
                  FROM roundmoves AS rm,
                       (SELECT rm1.roundid, MAX(rm1.nummove) AS maxmove
                        FROM roundmoves AS rm1
                        WHERE rm1.roundid = $1
                        GROUP BY rm1.roundid) AS rmm
                  WHERE rmm.roundid = rm.roundid AND rmm.maxmove = rm.nummove);
END;
$$ LANGUAGE 'plpgsql';

