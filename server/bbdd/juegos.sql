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

CREATE FUNCTION getactiverounds(gameid integer, playerid uuid) RETURNS TABLE(roundid integer, numberofplayers integer, dateevent timestamp with time zone, playernames text, codedboard character varying)
    LANGUAGE plpgsql
    AS $_$
DECLARE

BEGIN

    RETURN QUERY (SELECT rrpp.roundid,
                         rrpp.numberofplayers,
                         rrpp.datecreated,
                         rrpp.pnames,
                         rm.codedboard 
                  FROM  roundmoves AS rm,
                        (SELECT r.roundid, r.numberofplayers,r.datecreated, 
                                string_agg(p.playername, ',') AS pnames,
                                r.lastmovenumber
                        FROM    rounds AS r, 
                                roundplayers AS rp, 
                                players AS p,
                                (SELECT rrr.roundid
                                 FROM   rounds AS rrr, roundplayers AS rp2
                                 WHERE rrr.roundid=rp2.roundid AND
                                       rp2.playerid=$2
                                 GROUP BY rrr.roundid) AS roundsosplayer
                        WHERE   roundsosplayer.roundid=r.roundid AND
                                r.roundid=rp.roundid AND 
                                rp.playerid=p.playerid AND
                                r.gameid = $1 AND 
                                (r.estado='A' OR r.estado='C')
                        GROUP BY r.roundid) AS rrpp
                  WHERE rrpp.roundid = rm.roundid AND 
                        rm.nummove=rrpp.lastmovenumber);

END;
$_$;


ALTER FUNCTION public.getactiverounds(gameid integer, playerid uuid) OWNER TO alumnodb;

--
-- Name: getboardid(text); Type: FUNCTION; Schema: public; Owner: alumnodb
--

CREATE FUNCTION getboardid(text) RETURNS integer
    LANGUAGE plpgsql
    AS $_$
DECLARE
    bn ALIAS FOR $1;
    fila boards%ROWTYPE;
BEGIN
    SELECT * INTO fila FROM boards WHERE boardname=bn;
    RETURN fila.boardid;
END;
$_$;


ALTER FUNCTION public.getboardid(text) OWNER TO alumnodb;

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

CREATE FUNCTION getopenrounds(gameid integer) RETURNS TABLE(roundid integer, numberofplayers integer, dateevent timestamp with time zone, playernames text, codedboard character varying)
    LANGUAGE plpgsql
    AS $_$
DECLARE

BEGIN

    RETURN QUERY (SELECT rrpp.roundid,
                         rrpp.numberofplayers,
                         rrpp.datecreated,
                         rrpp.pnames,
                         rm.codedboard 
                  FROM  roundmoves AS rm, 
                        (SELECT r.roundid, r.numberofplayers,r.datecreated, 
                                string_agg(p.playername, ',') AS pnames
                        FROM    rounds as r, roundplayers as rp, players as p  
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
                         1+(lastmovenumber%numberofplayers) = b.turn)
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

BEGIN

    UPDATE rounds AS r
      SET estado = 'F' -- Se marca partida finalizada
    WHERE r.roundid = $1;
    
    RETURN 1;
END;
$_$;


ALTER FUNCTION public.setboardasfinished(roundid integer) OWNER TO alumnodb;

--
-- Name: topten(text); Type: FUNCTION; Schema: public; Owner: alumnodb
--

CREATE FUNCTION topten(text) RETURNS TABLE(usuario character varying, npiezas integer, duracion integer, fecha date)
    LANGUAGE plpgsql
    AS $_$
DECLARE
    bn ALIAS FOR $1;
BEGIN

RETURN query SELECT u.username as usuario, s.npiezas, s.duracion, s.fecha 
             FROM scores AS s, usuarios AS u, boards AS b 
             WHERE u.playerid=s.playerid AND b.boardid=s.boardid AND
                   b.boardname = bn
             ORDER BY s.npiezas ASC, s.duracion ASC, s.fecha DESC
             LIMIT 10;
END;
$_$;


ALTER FUNCTION public.topten(text) OWNER TO alumnodb;

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
        SET estado = 'F' -- Se marca partida finalizada si abandona un jugador 
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
    maxplayers integer NOT NULL
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
    description character varying(128)
);


ALTER TABLE public.players OWNER TO alumnodb;

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
    turn integer DEFAULT 0 NOT NULL,
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
-- Data for Name: games; Type: TABLE DATA; Schema: public; Owner: alumnodb
--

COPY games (gameid, gamename, gamedescription, minplayers, maxplayers) FROM stdin;
1	tres en raya	Juego de estrategia donde los haya	2	2
2	chachacha	Juego de turnos por antonomasia	1	100
\.


--
-- Name: games_gameid_seq; Type: SEQUENCE SET; Schema: public; Owner: alumnodb
--

SELECT pg_catalog.setval('games_gameid_seq', 2, true);


--
-- Data for Name: messages; Type: TABLE DATA; Schema: public; Owner: alumnodb
--

COPY messages (destinatario, mensaje, fecha, entregado, remitente) FROM stdin;
aa	hola                                                                                                                                        	2014-04-04 17:22:23.544216+02	f	6741b473-9f6c-484d-bc01-7763dc7e0cd7
aa	hola                                                                                                                                        	2014-04-04 17:23:05.228894+02	f	6741b473-9f6c-484d-bc01-7763dc7e0cd7
aa	hola                                                                                                                                        	2014-04-04 17:28:56.809086+02	f	6741b473-9f6c-484d-bc01-7763dc7e0cd7
\.


--
-- Data for Name: players; Type: TABLE DATA; Schema: public; Owner: alumnodb
--

COPY players (playerid, playername, password, description) FROM stdin;
2f5fc958-9436-4ad1-b9d8-16f44daf1617	alejandro	sierra	\N
0bb68fc9-c103-46cc-a0c9-0f71db1c56d3	ale	ale	\N
6d199595-e48b-463d-80f4-e67461fc6d87	a	a	\N
ff76d770-7a7b-40e3-b61e-cdd5647e28e1	aa	e	\N
6741b473-9f6c-484d-bc01-7763dc7e0cd7	jj	go	\N
\.


--
-- Data for Name: roundmessages; Type: TABLE DATA; Schema: public; Owner: alumnodb
--

COPY roundmessages (roundid, senderid, message, msgdate) FROM stdin;
11	6741b473-9f6c-484d-bc01-7763dc7e0cd7	hola	2014-04-04 17:30:44.129728+02
\.


--
-- Data for Name: roundmoves; Type: TABLE DATA; Schema: public; Owner: alumnodb
--

COPY roundmoves (roundid, nummove, codedboard, playerid, movedate) FROM stdin;
9	0		6741b473-9f6c-484d-bc01-7763dc7e0cd7	2014-04-02 00:00:00+02
10	0		6741b473-9f6c-484d-bc01-7763dc7e0cd7	2014-04-02 00:00:00+02
11	0		6741b473-9f6c-484d-bc01-7763dc7e0cd7	2014-04-02 17:46:35.296863+02
12	0		6741b473-9f6c-484d-bc01-7763dc7e0cd7	2014-04-02 18:09:05.999199+02
15	0		2f5fc958-9436-4ad1-b9d8-16f44daf1617	2014-04-02 18:34:54.985056+02
20	0		6741b473-9f6c-484d-bc01-7763dc7e0cd7	2014-04-02 19:04:08.06606+02
21	0		6741b473-9f6c-484d-bc01-7763dc7e0cd7	2014-04-03 10:20:12.865925+02
22	0		6741b473-9f6c-484d-bc01-7763dc7e0cd7	2014-04-03 10:28:28.917332+02
23	0		6741b473-9f6c-484d-bc01-7763dc7e0cd7	2014-04-03 10:28:33.221782+02
25	0		6741b473-9f6c-484d-bc01-7763dc7e0cd7	2014-04-03 10:28:57.029438+02
29	0		0bb68fc9-c103-46cc-a0c9-0f71db1c56d3	2014-04-03 13:04:04.070918+02
20	1	000000000	6741b473-9f6c-484d-bc01-7763dc7e0cd7	2014-04-03 14:40:38.39704+02
11	2	000000001	6741b473-9f6c-484d-bc01-7763dc7e0cd7	2014-04-03 14:51:58.383396+02
11	3	000000002	6741b473-9f6c-484d-bc01-7763dc7e0cd7	2014-04-03 14:54:10.505451+02
11	4	000000003	6741b473-9f6c-484d-bc01-7763dc7e0cd7	2014-04-03 14:54:14.836067+02
11	4	000000004	6741b473-9f6c-484d-bc01-7763dc7e0cd7	2014-04-03 14:56:51.557726+02
11	5	000000004	6741b473-9f6c-484d-bc01-7763dc7e0cd7	2014-04-03 15:10:59.168406+02
35	0	098123456	6741b473-9f6c-484d-bc01-7763dc7e0cd7	2014-04-03 16:25:43.339394+02
11	6	0000000012	6741b473-9f6c-484d-bc01-7763dc7e0cd7	2014-04-03 16:46:54.729572+02
11	7	0000000012	ff76d770-7a7b-40e3-b61e-cdd5647e28e1	2014-04-03 16:47:39.945776+02
36	0	0000000000100000111000001000000100000000000000000	0bb68fc9-c103-46cc-a0c9-0f71db1c56d3	2014-04-03 16:55:29.267696+02
36	1	0011100001110011111111111111111111100111000011100	0bb68fc9-c103-46cc-a0c9-0f71db1c56d3	2014-04-03 17:28:04.726056+02
37	0		ff76d770-7a7b-40e3-b61e-cdd5647e28e1	2014-04-03 18:05:42.041136+02
11	8	000000004	6741b473-9f6c-484d-bc01-7763dc7e0cd7	2014-04-04 17:58:03.520836+02
11	9	00011	ff76d770-7a7b-40e3-b61e-cdd5647e28e1	2014-04-04 18:01:19.358941+02
\.


--
-- Data for Name: roundplayers; Type: TABLE DATA; Schema: public; Owner: alumnodb
--

COPY roundplayers (roundid, playerid, turn) FROM stdin;
9	6741b473-9f6c-484d-bc01-7763dc7e0cd7	1
10	6741b473-9f6c-484d-bc01-7763dc7e0cd7	1
10	6741b473-9f6c-484d-bc01-7763dc7e0cd7	2
12	6741b473-9f6c-484d-bc01-7763dc7e0cd7	1
11	6741b473-9f6c-484d-bc01-7763dc7e0cd7	2
9	6741b473-9f6c-484d-bc01-7763dc7e0cd7	2
15	6741b473-9f6c-484d-bc01-7763dc7e0cd7	1
15	6741b473-9f6c-484d-bc01-7763dc7e0cd7	2
20	6741b473-9f6c-484d-bc01-7763dc7e0cd7	1
21	6741b473-9f6c-484d-bc01-7763dc7e0cd7	1
20	6741b473-9f6c-484d-bc01-7763dc7e0cd7	1
21	6741b473-9f6c-484d-bc01-7763dc7e0cd7	1
22	6741b473-9f6c-484d-bc01-7763dc7e0cd7	1
23	6741b473-9f6c-484d-bc01-7763dc7e0cd7	1
25	6741b473-9f6c-484d-bc01-7763dc7e0cd7	1
25	6741b473-9f6c-484d-bc01-7763dc7e0cd7	2
25	6741b473-9f6c-484d-bc01-7763dc7e0cd7	3
25	6741b473-9f6c-484d-bc01-7763dc7e0cd7	4
25	6741b473-9f6c-484d-bc01-7763dc7e0cd7	5
25	6741b473-9f6c-484d-bc01-7763dc7e0cd7	6
25	6741b473-9f6c-484d-bc01-7763dc7e0cd7	7
25	6741b473-9f6c-484d-bc01-7763dc7e0cd7	8
29	0bb68fc9-c103-46cc-a0c9-0f71db1c56d3	1
29	6741b473-9f6c-484d-bc01-7763dc7e0cd7	2
29	0bb68fc9-c103-46cc-a0c9-0f71db1c56d3	3
11	ff76d770-7a7b-40e3-b61e-cdd5647e28e1	1
31	6741b473-9f6c-484d-bc01-7763dc7e0cd7	1
32	6741b473-9f6c-484d-bc01-7763dc7e0cd7	1
35	6741b473-9f6c-484d-bc01-7763dc7e0cd7	1
36	0bb68fc9-c103-46cc-a0c9-0f71db1c56d3	1
36	0bb68fc9-c103-46cc-a0c9-0f71db1c56d3	2
36	0bb68fc9-c103-46cc-a0c9-0f71db1c56d3	3
37	ff76d770-7a7b-40e3-b61e-cdd5647e28e1	1
37	6741b473-9f6c-484d-bc01-7763dc7e0cd7	2
\.


--
-- Data for Name: rounds; Type: TABLE DATA; Schema: public; Owner: alumnodb
--

COPY rounds (roundid, gameid, lastchange, turn, datecreated, numberofplayers, estado, lastmovenumber) FROM stdin;
12	1	\N	0	2014-04-02 18:09:05.999199+02	1	O	0
10	1	\N	0	2014-04-02 00:00:00+02	2	C	0
9	1	\N	0	2014-04-02 00:00:00+02	2	C	0
15	2	2014-04-02 18:34:54.985056+02	0	2014-04-02 18:34:54.985056+02	2	A	0
21	1	2014-04-03 10:20:12.865925+02	0	2014-04-03 10:20:12.865925+02	2	C	0
22	1	2014-04-03 10:28:28.917332+02	0	2014-04-03 10:28:28.917332+02	1	O	0
23	2	2014-04-03 10:28:33.221782+02	0	2014-04-03 10:28:33.221782+02	1	A	0
25	2	2014-04-03 10:28:57.029438+02	0	2014-04-03 10:28:57.029438+02	8	A	0
29	2	2014-04-03 13:04:04.070918+02	0	2014-04-03 13:04:04.070918+02	3	A	0
20	1	2014-04-02 19:04:08.06606+02	0	2014-04-02 19:04:08.06606+02	2	C	1
31	1	2014-04-03 16:07:12.473657+02	0	2014-04-03 16:07:12.473657+02	1	O	0
32	1	2014-04-03 16:10:17.087517+02	0	2014-04-03 16:10:17.087517+02	1	O	-1
33	1	2014-04-03 16:23:16.173353+02	0	2014-04-03 16:23:16.173353+02	\N	O	-1
35	1	2014-04-03 16:25:43.339394+02	0	2014-04-03 16:25:43.339394+02	1	O	0
36	2	2014-04-03 17:28:04.726056+02	0	2014-04-03 16:55:29.267696+02	3	C	1
37	1	2014-04-03 18:05:42.041136+02	0	2014-04-03 18:05:42.041136+02	2	C	0
11	1	2014-04-04 18:01:19.358941+02	0	2014-04-02 17:46:35.296863+02	2	C	9
\.


--
-- Name: rounds_roundid_seq; Type: SEQUENCE SET; Schema: public; Owner: alumnodb
--

SELECT pg_catalog.setval('rounds_roundid_seq', 37, true);


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

