CREATE OR REPLACE FUNCTION getRoundHistory(roundid integer,desde timestamp with time zone) 
            RETURNS TABLE(quien character varying, 
                          que character varying(1024),
                          cuando timestamp with time zone,
                          tipo integer) AS
$$
DECLARE

BEGIN

    RETURN QUERY (SELECT * 
                  FROM (SELECT playername AS quien, 
                               message AS que, 
                               msgdate AS cuando, 
                               0 AS tipo 
                        FROM roundmessages rm, players p 
                        WHERE rm.senderid = p.playerid AND rm.roundid=$1 
                      UNION ALL 
                        SELECT playername AS quien, 
                               codedboard AS que,
                               movedate AS cuando,
                               1 AS tipo
                        FROM roundmoves rm, 
                             players p 
                        WHERE rm.playerid = p.playerid AND rm.roundid=$1) AS s 
                  WHERE s.cuando >= $2 
                  ORDER BY cuando);

END;
$$ LANGUAGE 'plpgsql';

