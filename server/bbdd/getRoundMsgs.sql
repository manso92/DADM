CREATE OR REPLACE FUNCTION getRoundMsgs(roundid integer, fromdate timestamp with time zone) 
            RETURNS TABLE(playername character varying, 
                          message character varying(1024),
                          msgdate timestamp with time zone) AS
$$
DECLARE

BEGIN

    RETURN QUERY (SELECT p.playername, rm.message, rm.msgdate
                  FROM   roundmessages AS rm, players AS p
                  WHERE  rm.senderid = p.playerid AND
                         rm.roundid=$1 AND
                         rm.msgdate>=$2
                  ORDER BY rm.msgdate);

END;
$$ LANGUAGE 'plpgsql';

