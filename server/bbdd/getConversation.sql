CREATE OR REPLACE FUNCTION getConversation(playerid uuid, pname character varying) 
            RETURNS TABLE(playername character varying, 
                          message character(140),
                          msgdate timestamp with time zone) AS
$$
DECLARE

BEGIN

    RETURN QUERY (SELECT ori.playername, mensaje, fecha
                  FROM   messages AS m, 
                         players  AS des, 
                         players  AS ori 
                  WHERE  (m.destinatario = des.playername AND
                         des.playerid = $1 AND
                         ori.playername = $2 AND
                         m.remitente = ori.playerid) OR
                           (m.destinatario = des.playername AND
                            des.playername = $2 AND
                            ori.playerid = $1 AND
                            m.remitente = ori.playerid)
                         );

END;
$$ LANGUAGE 'plpgsql';

