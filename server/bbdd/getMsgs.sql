CREATE OR REPLACE FUNCTION getMsgs(playerid uuid) 
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
                  WHERE  m.destinatario = des.playername AND
                         des.playerid = $1 AND
                         m.remitente = ori.playerid AND
                         m.entregado = false
                  ORDER BY fecha);

END;
$$ LANGUAGE 'plpgsql';

