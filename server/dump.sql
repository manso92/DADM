--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


--
-- Name: uuid-ossp; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS "uuid-ossp" WITH SCHEMA public;


--
-- Name: EXTENSION "uuid-ossp"; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION "uuid-ossp" IS 'generate universally unique identifiers (UUIDs)';


SET search_path = public, pg_catalog;

--
-- Name: getactiverounds(integer, uuid); Type: FUNCTION; Schema: public; Owner: alumnodb
--

CREATE FUNCTION getactiverounds(gameid integer, playerid uuid) RETURNS TABLE(roundid integer, numberofplayers integer, dateevent timestamp with time zone, playernames text, turn integer, codedboard character varying)
    LANGUAGE plpgsql
    AS $_$
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
$_$;


ALTER FUNCTION public.getactiverounds(gameid integer, playerid uuid) OWNER TO alumnodb;

--
-- Name: getconversation(uuid, character varying); Type: FUNCTION; Schema: public; Owner: alumnodb
--

CREATE FUNCTION getconversation(playerid uuid, pname character varying) RETURNS TABLE(playername character varying, message character, msgdate timestamp with time zone)
    LANGUAGE plpgsql
    AS $_$
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
$_$;


ALTER FUNCTION public.getconversation(playerid uuid, pname character varying) OWNER TO alumnodb;

--
-- Name: getconversationusers(uuid); Type: FUNCTION; Schema: public; Owner: alumnodb
--

CREATE FUNCTION getconversationusers(playerid uuid) RETURNS TABLE(playername character varying, msgdate timestamp with time zone)
    LANGUAGE plpgsql
    AS $_$
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
$_$;


ALTER FUNCTION public.getconversationusers(playerid uuid) OWNER TO alumnodb;

--
-- Name: getfinishedrounds(integer, uuid); Type: FUNCTION; Schema: public; Owner: alumnodb
--

CREATE FUNCTION getfinishedrounds(gameid integer, playerid uuid) RETURNS TABLE(roundid integer, numberofplayers integer, dateevent timestamp with time zone, playernames text, turn integer, codedboard character varying)
    LANGUAGE plpgsql
    AS $_$
DECLARE

BEGIN

    RETURN QUERY (SELECT uurr.roundid,
                         uurr.numberofplayers,
                         uurr.dateevent,
                         uurr.playernames,
                         uurr.turn,
                         uurr.codedboard 
                  FROM  (SELECT * FROM getUserRounds($1,$2)) AS uurr
                  WHERE uurr.estado='F');

END;
$_$;


ALTER FUNCTION public.getfinishedrounds(gameid integer, playerid uuid) OWNER TO alumnodb;

--
-- Name: getlastcodedboard(integer); Type: FUNCTION; Schema: public; Owner: alumnodb
--

CREATE FUNCTION getlastcodedboard(round_id integer) RETURNS TABLE(roundid integer, nummove integer, codedboard character varying, playerid uuid, movedate timestamp with time zone)
    LANGUAGE plpgsql
    AS $_$
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
$_$;


ALTER FUNCTION public.getlastcodedboard(round_id integer) OWNER TO alumnodb;

--
-- Name: getmsgs(uuid); Type: FUNCTION; Schema: public; Owner: alumnodb
--

CREATE FUNCTION getmsgs(playerid uuid) RETURNS TABLE(playername character varying, message character, msgdate timestamp with time zone)
    LANGUAGE plpgsql
    AS $_$
DECLARE

BEGIN

    RETURN QUERY (SELECT ori.playername, mensaje, fecha
                  FROM   messages AS m, 
                         players  AS des, 
                         players  AS ori 
                  WHERE  m.destinatario = des.playername AND
                         des.playerid = $1 AND
                         m.remitente = ori.playerid AND
                         m.entregado = false);

END;
$_$;


ALTER FUNCTION public.getmsgs(playerid uuid) OWNER TO alumnodb;

--
-- Name: getnextmovenumber(integer); Type: FUNCTION; Schema: public; Owner: alumnodb
--

CREATE FUNCTION getnextmovenumber(roundid integer) RETURNS integer
    LANGUAGE plpgsql
    AS $_$
DECLARE
  nummoves integer;
BEGIN

    SELECT r.lastmovenumber+1 INTO nummoves 
    FROM rounds AS r
    WHERE r.roundid = $1;

    RETURN nummoves;
END;
$_$;


ALTER FUNCTION public.getnextmovenumber(roundid integer) OWNER TO alumnodb;

--
-- Name: getnextplayer(integer); Type: FUNCTION; Schema: public; Owner: alumnodb
--

CREATE FUNCTION getnextplayer(roundid integer) RETURNS integer
    LANGUAGE plpgsql
    AS $_$
DECLARE
  numplayers integer;
BEGIN

    SELECT count(*) INTO numplayers 
    FROM roundplayers AS rp
    WHERE rp.roundid = $1;

    numplayers := numplayers + 1;

    IF EXISTS(SELECT g.gameid 
              FROM rounds AS r, games AS g
              WHERE r.roundid = $1 AND r.gameid=g.gameid AND
                    g.maxplayers < numplayers)
    THEN
        RETURN -1; --Si superamos el numero max de jugadores
    END IF;

    RETURN numplayers;
END;
$_$;


ALTER FUNCTION public.getnextplayer(roundid integer) OWNER TO alumnodb;

--
-- Name: getopenrounds(integer); Type: FUNCTION; Schema: public; Owner: alumnodb
--

CREATE FUNCTION getopenrounds(gameid integer) RETURNS TABLE(roundid integer, numberofplayers integer, dateevent timestamp with time zone, playernames text, turn integer, codedboard character varying)
    LANGUAGE plpgsql
    AS $_$
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
$_$;


ALTER FUNCTION public.getopenrounds(gameid integer) OWNER TO alumnodb;

--
-- Name: getresults(integer); Type: FUNCTION; Schema: public; Owner: alumnodb
--

CREATE FUNCTION getresults(gameid integer) RETURNS TABLE(playername character varying, timeround integer, points integer, otherinfo character varying)
    LANGUAGE plpgsql
    AS $_$
DECLARE

BEGIN

    RETURN QUERY (SELECT p.playername,
                         r.roundtime,
                         r.points,
                         r.otherinfo
                  FROM  results AS r,
                        players AS p
                  WHERE p.playerid=r.playerid AND
                        r.gameid=$1);

END;
$_$;


ALTER FUNCTION public.getresults(gameid integer) OWNER TO alumnodb;

--
-- Name: getresultsgrouped(integer); Type: FUNCTION; Schema: public; Owner: alumnodb
--

CREATE FUNCTION getresultsgrouped(gameid integer) RETURNS TABLE(playername character varying, timeround bigint, points bigint)
    LANGUAGE plpgsql
    AS $_$
DECLARE

BEGIN

    RETURN QUERY (SELECT p.playername,
                         SUM(r.roundtime),
                         SUM(r.points)
                  FROM  results AS r,
                        players AS p
                  WHERE p.playerid=r.playerid AND
                        r.gameid=$1
                  GROUP BY p.playername);

END;
$_$;


ALTER FUNCTION public.getresultsgrouped(gameid integer) OWNER TO alumnodb;

--
-- Name: getroundhistory(integer, timestamp with time zone); Type: FUNCTION; Schema: public; Owner: alumnodb
--

CREATE FUNCTION getroundhistory(roundid integer, desde timestamp with time zone) RETURNS TABLE(quien character varying, que character varying, cuando timestamp with time zone, tipo integer)
    LANGUAGE plpgsql
    AS $_$
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
$_$;


ALTER FUNCTION public.getroundhistory(roundid integer, desde timestamp with time zone) OWNER TO alumnodb;

--
-- Name: getroundmsgs(integer, timestamp with time zone); Type: FUNCTION; Schema: public; Owner: alumnodb
--

CREATE FUNCTION getroundmsgs(roundid integer, fromdate timestamp with time zone) RETURNS TABLE(playername character varying, message character varying, msgdate timestamp with time zone)
    LANGUAGE plpgsql
    AS $_$
DECLARE

BEGIN

    RETURN QUERY (SELECT p.playername, rm.message, rm.msgdate
                  FROM   roundmessages AS rm, players AS p
                  WHERE  rm.senderid = p.playerid AND
                         rm.roundid=$1 AND
                         rm.msgdate>=$2);

END;
$_$;


ALTER FUNCTION public.getroundmsgs(roundid integer, fromdate timestamp with time zone) OWNER TO alumnodb;

--
-- Name: getuserrounds(integer, uuid); Type: FUNCTION; Schema: public; Owner: alumnodb
--

CREATE FUNCTION getuserrounds(gameid integer, playerid uuid) RETURNS TABLE(roundid integer, numberofplayers integer, dateevent timestamp with time zone, playernames text, turn integer, codedboard character varying, estado "char")
    LANGUAGE plpgsql
    AS $_$
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
$_$;


ALTER FUNCTION public.getuserrounds(gameid integer, playerid uuid) OWNER TO alumnodb;

--
-- Name: inround(integer, uuid); Type: FUNCTION; Schema: public; Owner: alumnodb
--

CREATE FUNCTION inround(roundid integer, playerid uuid) RETURNS integer
    LANGUAGE plpgsql
    AS $_$
DECLARE

BEGIN

    IF NOT EXISTS(SELECT b.playerid
                  FROM   rounds a, roundplayers b
                  WHERE  a.roundid  = $1        AND 
                         a.roundid  = b.roundid AND
                         b.playerid = $2) 
    THEN
        RETURN 0; -- No estas en esa partida
    END IF;


    RETURN 1; --Si estas
END;
$_$;


ALTER FUNCTION public.inround(roundid integer, playerid uuid) OWNER TO alumnodb;

--
-- Name: ismyturn(integer, uuid); Type: FUNCTION; Schema: public; Owner: alumnodb
--

CREATE FUNCTION ismyturn(roundid integer, playerid uuid) RETURNS integer
    LANGUAGE plpgsql
    AS $_$
DECLARE
  numplayers integer;
BEGIN

    IF NOT EXISTS(SELECT r.roundid 
                  FROM   rounds AS r
                  WHERE  r.roundid = $1 AND 
                         (r.estado = 'A' OR r.estado = 'C' ) )
    THEN
        RETURN -1; -- Partida no lista para jugar o no existe
    END IF;

    IF NOT EXISTS(SELECT b.playerid
                  FROM   rounds a, roundplayers b
                  WHERE  a.roundid  = $1        AND 
                         a.roundid  = b.roundid AND
                         b.playerid = $2        AND 
                         a.turn = b.turn)
--                         1+(lastmovenumber%numberofplayers) = b.turn)
    THEN
        RETURN 0; -- No te toca
    END IF;


    RETURN 1;
END;
$_$;


ALTER FUNCTION public.ismyturn(roundid integer, playerid uuid) OWNER TO alumnodb;

--
-- Name: setboardasfinished(integer); Type: FUNCTION; Schema: public; Owner: alumnodb
--

CREATE FUNCTION setboardasfinished(roundid integer) RETURNS integer
    LANGUAGE plpgsql
    AS $_$
DECLARE
    rows integer;
BEGIN

    UPDATE rounds AS r
      SET estado = 'F' -- Se marca partida finalizada
    WHERE r.roundid = $1;
    
    GET DIAGNOSTICS rows = ROW_COUNT;

    RETURN rows;

END;
$_$;


ALTER FUNCTION public.setboardasfinished(roundid integer) OWNER TO alumnodb;

--
-- Name: updroundmoves(); Type: FUNCTION; Schema: public; Owner: alumnodb
--

CREATE FUNCTION updroundmoves() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
  numplayers integer;
BEGIN

      RAISE NOTICE '%', NEW.roundid;

      IF EXISTS(SELECT r.roundid 
                FROM rounds AS r
                WHERE r.roundid = NEW.roundid AND 
                      r.estado <> 'A' AND r.estado <> 'C' AND lastmovenumber>-1)
      THEN
          RETURN NULL; --Si la partida ya esta marcada como finalizada (F) o cerrada (C) 
      END IF;

      UPDATE rounds AS r
        SET  estado = CASE
                WHEN r.lastmovenumber < 0 THEN r.estado
                ELSE 'C'
             END,
--             turn = 1+((lastmovenumber+1)%numberofplayers),
             turn = 1+(turn%numberofplayers),
             lastmovenumber = lastmovenumber+1,
             lastchange = now()
        WHERE r.roundid = NEW.roundid;

  RETURN NEW;

END
$$;


ALTER FUNCTION public.updroundmoves() OWNER TO alumnodb;

--
-- Name: updroundplayers(); Type: FUNCTION; Schema: public; Owner: alumnodb
--

CREATE FUNCTION updroundplayers() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.updroundplayers() OWNER TO alumnodb;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: games; Type: TABLE; Schema: public; Owner: alumnodb; Tablespace: 
--

CREATE TABLE games (
    gameid integer NOT NULL,
    gamename character varying(128) NOT NULL,
    gamedescription character varying(256),
    minplayers integer NOT NULL,
    maxplayers integer NOT NULL,
    owner character varying(128)
);


ALTER TABLE public.games OWNER TO alumnodb;

--
-- Name: games_gameid_seq; Type: SEQUENCE; Schema: public; Owner: alumnodb
--

CREATE SEQUENCE games_gameid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.games_gameid_seq OWNER TO alumnodb;

--
-- Name: games_gameid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: alumnodb
--

ALTER SEQUENCE games_gameid_seq OWNED BY games.gameid;


--
-- Name: messages; Type: TABLE; Schema: public; Owner: alumnodb; Tablespace: 
--

CREATE TABLE messages (
    destinatario character varying NOT NULL,
    mensaje character(140),
    fecha timestamp with time zone DEFAULT now(),
    entregado boolean DEFAULT false,
    remitente uuid
);


ALTER TABLE public.messages OWNER TO alumnodb;

--
-- Name: players; Type: TABLE; Schema: public; Owner: alumnodb; Tablespace: 
--

CREATE TABLE players (
    playerid uuid NOT NULL,
    playername character varying NOT NULL,
    password character varying NOT NULL,
    description character varying(128),
    gcmregid character varying(1024)
);


ALTER TABLE public.players OWNER TO alumnodb;

--
-- Name: results; Type: TABLE; Schema: public; Owner: alumnodb; Tablespace: 
--

CREATE TABLE results (
    playerid uuid,
    gameid integer,
    roundtime integer,
    points integer,
    otherinfo character varying(2048)
);


ALTER TABLE public.results OWNER TO alumnodb;

--
-- Name: roundmessages; Type: TABLE; Schema: public; Owner: alumnodb; Tablespace: 
--

CREATE TABLE roundmessages (
    roundid integer,
    senderid uuid,
    message character varying(1024),
    msgdate timestamp with time zone DEFAULT now()
);


ALTER TABLE public.roundmessages OWNER TO alumnodb;

--
-- Name: roundmoves; Type: TABLE; Schema: public; Owner: alumnodb; Tablespace: 
--

CREATE TABLE roundmoves (
    roundid integer,
    nummove integer DEFAULT 0,
    codedboard character varying(1024),
    playerid uuid,
    movedate timestamp with time zone DEFAULT now()
);


ALTER TABLE public.roundmoves OWNER TO alumnodb;

--
-- Name: roundplayers; Type: TABLE; Schema: public; Owner: alumnodb; Tablespace: 
--

CREATE TABLE roundplayers (
    roundid integer,
    playerid uuid,
    turn integer
);


ALTER TABLE public.roundplayers OWNER TO alumnodb;

--
-- Name: rounds; Type: TABLE; Schema: public; Owner: alumnodb; Tablespace: 
--

CREATE TABLE rounds (
    roundid integer NOT NULL,
    gameid integer,
    lastchange timestamp with time zone DEFAULT now(),
    turn integer DEFAULT 1 NOT NULL,
    datecreated timestamp with time zone DEFAULT now(),
    numberofplayers integer,
    estado "char" DEFAULT 'O'::"char" NOT NULL,
    lastmovenumber integer DEFAULT (-1) NOT NULL
);


ALTER TABLE public.rounds OWNER TO alumnodb;

--
-- Name: rounds_roundid_seq; Type: SEQUENCE; Schema: public; Owner: alumnodb
--

CREATE SEQUENCE rounds_roundid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.rounds_roundid_seq OWNER TO alumnodb;

--
-- Name: rounds_roundid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: alumnodb
--

ALTER SEQUENCE rounds_roundid_seq OWNED BY rounds.roundid;


--
-- Name: gameid; Type: DEFAULT; Schema: public; Owner: alumnodb
--

ALTER TABLE ONLY games ALTER COLUMN gameid SET DEFAULT nextval('games_gameid_seq'::regclass);


--
-- Name: roundid; Type: DEFAULT; Schema: public; Owner: alumnodb
--

ALTER TABLE ONLY rounds ALTER COLUMN roundid SET DEFAULT nextval('rounds_roundid_seq'::regclass);


--
-- Name: games_pkey; Type: CONSTRAINT; Schema: public; Owner: alumnodb; Tablespace: 
--

ALTER TABLE ONLY games
    ADD CONSTRAINT games_pkey PRIMARY KEY (gameid);


--
-- Name: rounds_pkey; Type: CONSTRAINT; Schema: public; Owner: alumnodb; Tablespace: 
--

ALTER TABLE ONLY rounds
    ADD CONSTRAINT rounds_pkey PRIMARY KEY (roundid);


--
-- Name: user_pkey; Type: CONSTRAINT; Schema: public; Owner: alumnodb; Tablespace: 
--

ALTER TABLE ONLY players
    ADD CONSTRAINT user_pkey PRIMARY KEY (playerid);


--
-- Name: user_username_key; Type: CONSTRAINT; Schema: public; Owner: alumnodb; Tablespace: 
--

ALTER TABLE ONLY players
    ADD CONSTRAINT user_username_key UNIQUE (playername);


--
-- Name: t_updroundmoves; Type: TRIGGER; Schema: public; Owner: alumnodb
--

CREATE TRIGGER t_updroundmoves BEFORE INSERT ON roundmoves FOR EACH ROW EXECUTE PROCEDURE updroundmoves();


--
-- Name: t_updroundplayers; Type: TRIGGER; Schema: public; Owner: alumnodb
--

CREATE TRIGGER t_updroundplayers BEFORE INSERT OR DELETE ON roundplayers FOR EACH ROW EXECUTE PROCEDURE updroundplayers();


--
-- Name: mensajes_destinatario_fkey; Type: FK CONSTRAINT; Schema: public; Owner: alumnodb
--

ALTER TABLE ONLY messages
    ADD CONSTRAINT mensajes_destinatario_fkey FOREIGN KEY (destinatario) REFERENCES players(playername);


--
-- Name: mensajes_remitente_fkey; Type: FK CONSTRAINT; Schema: public; Owner: alumnodb
--

ALTER TABLE ONLY messages
    ADD CONSTRAINT mensajes_remitente_fkey FOREIGN KEY (remitente) REFERENCES players(playerid);


--
-- Name: results_gameid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: alumnodb
--

ALTER TABLE ONLY results
    ADD CONSTRAINT results_gameid_fkey FOREIGN KEY (gameid) REFERENCES games(gameid);


--
-- Name: results_playerid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: alumnodb
--

ALTER TABLE ONLY results
    ADD CONSTRAINT results_playerid_fkey FOREIGN KEY (playerid) REFERENCES players(playerid);


--
-- Name: roundmessages_playerid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: alumnodb
--

ALTER TABLE ONLY roundmessages
    ADD CONSTRAINT roundmessages_playerid_fkey FOREIGN KEY (senderid) REFERENCES players(playerid);


--
-- Name: roundmessages_roundid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: alumnodb
--

ALTER TABLE ONLY roundmessages
    ADD CONSTRAINT roundmessages_roundid_fkey FOREIGN KEY (roundid) REFERENCES rounds(roundid);


--
-- Name: roundmoves_playerid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: alumnodb
--

ALTER TABLE ONLY roundmoves
    ADD CONSTRAINT roundmoves_playerid_fkey FOREIGN KEY (playerid) REFERENCES players(playerid);


--
-- Name: roundmoves_roundid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: alumnodb
--

ALTER TABLE ONLY roundmoves
    ADD CONSTRAINT roundmoves_roundid_fkey FOREIGN KEY (roundid) REFERENCES rounds(roundid);


--
-- Name: roundplayers_playerid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: alumnodb
--

ALTER TABLE ONLY roundplayers
    ADD CONSTRAINT roundplayers_playerid_fkey FOREIGN KEY (playerid) REFERENCES players(playerid);


--
-- Name: roundplayers_roundid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: alumnodb
--

ALTER TABLE ONLY roundplayers
    ADD CONSTRAINT roundplayers_roundid_fkey FOREIGN KEY (roundid) REFERENCES rounds(roundid);


--
-- Name: rounds_gameid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: alumnodb
--

ALTER TABLE ONLY rounds
    ADD CONSTRAINT rounds_gameid_fkey FOREIGN KEY (gameid) REFERENCES games(gameid);


--
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- PostgreSQL database dump complete
--

