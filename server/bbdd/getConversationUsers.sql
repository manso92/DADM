CREATE OR REPLACE FUNCTION getConversationUsers(playerid uuid) 
            RETURNS TABLE(playername character varying, 
                          msgdate timestamp with time zone) AS
$$
DECLARE

BEGIN

    RETURN QUERY (SELECT name, max(f) AS fecha 
                  FROM (SELECT destinatario AS name, fecha AS f 
                        FROM   messages 
                        WHERE  remitente=$1
                              UNION 
                        SELECT rem.playername AS name, m.fecha AS f 
                        FROM   players AS des, players AS rem, messages AS m 
                        WHERE  des.playerid   = $1 AND 
                               des.playername = m.destinatario AND 
                               m.remitente    = rem.playerid) AS conversations 
                        GROUP BY name
                        ORDER BY fecha DESC);

END;
$$ LANGUAGE 'plpgsql';

