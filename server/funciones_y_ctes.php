<?php

/*   funciones_y_ctes.php: pues eso
 * 
 *   Copyright (C) 2014  Gonzalo Martinez Munoz, David Arroyo, Alejandro Sierra
 *                       Universidad Autónoma de Madrid
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see http://www.gnu.org/licenses/ .
 */

define("LOGFILE", "log");

define("PGUSER", "alumnodb2");
define("PGPASSWORD", "alumnodb2");
define("DSN", "pgsql:host=127.0.0.1;dbname=juegos2017;options='--client_encoding=UTF8'");

define("MSG_NEWMOVEMENT", "1");
define("MSG_USER", "2");
define("MSG_TOROUND", "3");


/**
 * 
 */
abstract class RoundStates {

    const Active = 0;
    const Open = 1;
    const Finished = 2;

}
/**
 *  Envía los mesanges a los jugadores de una partida, excepto al emisor
 */
function send_gcmmessage_to_round($roundid,$message,$playerid,$type=MSG_NEWMOVEMENT) {
    
    $db = nueva_conexion_db();

    try {
        /*$q = $db->query("SELECT p.* FROM players AS p, ".
                                           "roundplayers as rp, ".
                                           "rounds as r ".
                           "WHERE rp.roundid = r.roundid AND ".
                                 "p.playerid = rp.playerid AND ".
                                 "r.roundid=".$roundid." AND ".
                                 "p.playerid<>'".$playerid."';");*/
        $q = $db->prepare("SELECT p.* FROM players AS p, ".
                                           "roundplayers as rp, ".
                                           "rounds as r ".
                           "WHERE rp.roundid = r.roundid AND ".
                                 "p.playerid = rp.playerid AND ".
                                 "r.roundid=:roundid AND ".
                                 "p.playerid<>:playerid;");
        $q->bindParam(":playerid", $playerid, PDO::PARAM_STR);
        $q->bindParam(":roundid", $roundid, PDO::PARAM_INT);
        $q->execute();

        $listamoviles = array();
        while (FALSE != ($row = $q->fetch())) {
            if ($row['gcmregid'] != NULL && $row['gcmregid'] != '') {
                array_push($listamoviles, $row['gcmregid']);
            }
        }
        if (count($listamoviles)>0) {
            file_put_contents (get_log_filename(), "GCM: ".$message, FILE_APPEND);
            $message = array('msgtype' => utf8_encode($type), 
                             'sender' => utf8_encode($roundid), 
                             'content' => utf8_encode($message));
            enviaMensajePush($message, $listamoviles);
        }
        
    } catch (PDOException $e) {
        //echo "Error en DB: " . $e->getMessage();
        file_put_contents (get_log_filename(), "".$e->getMessage(), FILE_APPEND);
    }
    $db = null;
}


function send_gcmmessage_to_player($playername,$message,$from="") {
    
    $db = nueva_conexion_db();

    try {
        $q = $db->prepare("SELECT * FROM players AS p ".
                          "WHERE p.playername=:playername;");
        $q->bindParam(":playername", $playername, PDO::PARAM_STR);
        $q->execute();

        $listamoviles = array();
        while (FALSE != ($row = $q->fetch())) {
            if ($row['gcmregid'] != NULL && $row['gcmregid'] != '') {
                array_push($listamoviles, $row['gcmregid']);
            }
        }
        if (count($listamoviles)>0) {
            $message = array('msgtype' => utf8_encode(MSG_USER), 
                             'sender' => utf8_encode($from), 
                             'content' => utf8_encode($message));
            enviaMensajePush($message, $listamoviles);
        }
        
    } catch (PDOException $e) {
        echo "Error en DB: " . $e->getMessage();
    }
    $db = null;
}

/**
 * 
 * @return log file name
 */
function get_log_filename() {
    $playerid = isPlayerIdAccesible() ? getPlayerId() : 'general';
    return 'logs/'.LOGFILE.'_'.$playerid.'.txt';
}
/**
 * 
 * @return \PDO
 */
function nueva_conexion_db() {
    $conexion = new PDO(DSN, PGUSER, PGPASSWORD);
    $conexion->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    return $conexion;
}

/**
 * Devuelve si el playerid esta accesible. Por el momento permite
 *    conexion con sesion o por parametro. Esto ultimo es poco seguro.
 *    Para que solo permita acceso por sesion cambiar el return por el comentado  
 * 
 * @return boolean
 */
function isPlayerIdAccesible() {
    return isset($_REQUEST["playerid"]) || isset($_SESSION["playerid"]);
//    return isset($_SESSION["playerid"]);
}

/**
 * Devuelve el playerid si esta accesible. Por el momento lo recupera
 *    de la sesion o del request. Esto ultimo es poco seguro.
 *    Para que solo permita acceso por sesion cambiar el return por el comentado  
 * 
 * @return playeid
 */
function getPlayerId() {
    if (!isPlayerIdAccesible())
        return FALSE;

    return isset($_REQUEST["playerid"]) ? $_REQUEST["playerid"] :
            $_SESSION["playerid"];
//    return $_SESSION["playerid"];
}

/**
 * 
 * @param type $roundstate
 */
function getrounds($gameid, $roundstate) {

    $db = nueva_conexion_db();

    $playerid = isPlayerIdAccesible() ? getPlayerId() : '';

    try {

        if ($roundstate == RoundStates::Open) {
            $q = $db->prepare("SELECT * FROM getOpenRounds(:gameid);");
        } elseif ($roundstate == RoundStates::Active) {
            $q = $db->prepare("SELECT * FROM getActiveRounds(:gameid,:playerid);");
            $q->bindParam(":playerid", $playerid, PDO::PARAM_STR);
        } else /*if ($roundstate == RoundStates::Finished)*/ {
            $q = $db->prepare("SELECT * FROM getFinishedRounds(:gameid,:playerid);");
            $q->bindParam(":playerid", $playerid, PDO::PARAM_STR);
        }
        $q->bindParam(":gameid", $gameid, PDO::PARAM_INT);
        $q->execute();

        if (!isset($_REQUEST["xml"])) {
            $rr = array();
            for ($i = 0; FALSE != ($row = $q->fetch(PDO::FETCH_ASSOC)); ++$i) {
                $rr[] = $row;
            }
            file_put_contents(get_log_filename(), "    En json!\n", FILE_APPEND);
            print json_encode($rr);
        } else {
            $xml = new SimpleXMLElement('<xml/>');

            while (FALSE != ($row = $q->fetch())) {
                $round = $xml->addChild('round');
                $round->addAttribute('roundid', $row['roundid']);
                //$round->addChild('roundid',           $row['roundid']);
                //$round->addChild('numberofplayers',   $row['numberofplayers']);
                $pn = $round->addChild('playernames', $row['playernames']);
                $pn->addAttribute('numberofplayers', $row['numberofplayers']);
                $round->addChild('turn', $row['turn']);
                $round->addChild('dateevent', $row['dateevent']);
                $round->addChild('codedboard', $row['codedboard']);
            }

            Header('Content-type: text/xml');
            file_put_contents(get_log_filename(), "\n", FILE_APPEND);
            print($xml->asXML());
        }
    } catch (PDOException $e) {
        echo "Error en DB: " . $e->getMessage();
    }
    $db = null;
}

/*
 * 
 * @param type game y gruped
 */
function getresults($gameid, $groupby) {

    $db = nueva_conexion_db();

    try {

        
        $q = $groupby ? $db->prepare("SELECT * FROM getResultsGrouped(:gameid);") :
                        $db->prepare("SELECT * FROM getResults(:gameid);");
        
        $q->bindParam(":gameid", $gameid, PDO::PARAM_INT);

        $q->execute();
        
        if (!isset($_REQUEST["xml"])) {
            $rr = array();
            for ($i = 0; FALSE != ($row = $q->fetch(PDO::FETCH_ASSOC)); ++$i) {
                $rr[] = $row;
            }
            file_put_contents(get_log_filename(), "    En json!\n", FILE_APPEND);
            print json_encode($rr);
        } else {
            $xml = new SimpleXMLElement('<xml/>');

            while (FALSE != ($row = $q->fetch())) {
                $round = $xml->addChild('result');
                $round->addChild('playername', $row['playername']);
                $round->addChild('roundtime', $row['roundtime']);
                $round->addChild('points', $row['points']);
                if (!$groupby) $round->addChild('otherinfo', $row['otherinfo']);
                else           $round->addChild('otherinfo', $row['otherinfo']);
            }

            Header('Content-type: text/xml');
            file_put_contents(get_log_filename(), "\n", FILE_APPEND);
            print($xml->asXML());
        }
    } catch (PDOException $e) {
        echo "Error en DB: " . $e->getMessage();
    }
    $db = null;
}


/**
 * Envia un mensaje a la lista de moviles especificada
 * @param type msg mensaje que se desea enviar
 * @param type $listMobiles lista de moviles a los que enviar el mensaje
 * @return
 * Por cortesia de  Miguel Rojo Esteva 
 */

function enviaMensajePush($msg, $listMobiles) {
    //enviaMensajePushGCM($msg, $listMobiles);
    enviaMensajePushFirebase($msg, $listMobiles);
}

function enviaMensajePushGCM($msg, $listMobiles) {
    
    //Browser key: clave para la comunicacion con el servidor de google message
    $apiKey = "AIzaSyD5bcsWjbp8J4xAm5WH2BkS3R4jl9TyN8k";
    
    // Datos
    if (!is_array($msg)) {
        $msg = array('mensaje' => utf8_encode($msg));
    }

    //Collapse_key: identidicador del mensaje. Como pretendo que sea unico para 
    //  que lleguen todos los mensajes y que no se acumulen (se podrian perder 
    //  mensajer) uso la fecha en milisegundos
    $collapseKey = (string) microtime();

    //Generacion de los datos para crear json: mensaje, id y lista de moviles 
    //  a la que se enviara
    $data = array(
        'data' => $msg,
        'collapse_key' => $collapseKey,
        'registration_ids' => $listMobiles
    );

    // Petición
    $ch = curl_init();

    //Direccion url a capturar
    curl_setopt($ch, CURLOPT_URL, "https://android.googleapis.com/gcm/send");
    $inf = curl_getinfo($ch);
    
    //Request Post method
    curl_setopt( $ch, CURLOPT_POST, true );
    
    //Configuracion de la cabecera http
    $headers = array('Content-Type:application/json', "Authorization:key = $apiKey");
    curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
    $inf = curl_getinfo($ch);

    //Comprobar que existe un nombre comun en el peer: comn 0 no se comprueba
    //curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, 0);
    //curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, 0);

    //TRUE para devolver el resultado de la transferencia como string del valor 
    //  de curl_exec()
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

    //Todos los datos para enviar vía HTTP "POST"
    curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($data));

    $inf = curl_getinfo($ch);
    
    // Conectamos y recuperamos la respuesta
    $response = curl_exec($ch);

    // Cerramos conexión
    curl_close($ch);
 
    file_put_contents (get_log_filename(), "\nGCM push sent. Response:>".$response."<\n", FILE_APPEND);
    
    return $response;
}

function enviaMensajePushFirebase($msg, $listMobiles) {
    
    //Browser key: clave para la comunicacion con el servidor de google message
    $apiKey = "AAAA3Nxgu3A:APA91bFZEjqsQCztuu6SlvER3DlFLC8Qo1PFW3uoqSZnpxiN1TkMFT01eHZsyvNj16TlykGmQA-hIVJkehwAtFuZxtUygA54GP039ohFqIJHTPtHY9pi_U2Ij5s-fW6B_xePLiOqlHJU";
    
    // Datos
    if (!is_array($msg)) {
        $msg = array('mensaje' => utf8_encode($msg));
    }

    //Collapse_key: identidicador del mensaje. Como pretendo que sea unico para 
    //  que lleguen todos los mensajes y que no se acumulen (se podrian perder 
    //  mensajer) uso la fecha en milisegundos
    $collapseKey = (string) microtime();

    //Generacion de los datos para crear json: mensaje, id y lista de moviles 
    //  a la que se enviara
    $data = array(
        'data' => $msg,
        'collapse_key' => $collapseKey,
//        'to' => $listMobiles[0]
        'registration_ids' => $listMobiles
    );

    // Petición
    $ch = curl_init();

    //Direccion url a capturar
    curl_setopt($ch, CURLOPT_URL, "https://fcm.googleapis.com/fcm/send");
    $inf = curl_getinfo($ch);
    
    //Request Post method
    curl_setopt( $ch, CURLOPT_POST, true );
    
    //Configuracion de la cabecera http
    $headers = array('Content-Type:application/json', "Authorization:key = $apiKey");
    curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
    $inf = curl_getinfo($ch);

    //Comprobar que existe un nombre comun en el peer: comn 0 no se comprueba
    //curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, 0);
    //curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, 0);

    //TRUE para devolver el resultado de la transferencia como string del valor 
    //  de curl_exec()
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

    //Todos los datos para enviar vía HTTP "POST"
    curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($data));

    $inf = curl_getinfo($ch);
    
    // Conectamos y recuperamos la respuesta
    $response = curl_exec($ch);

    // Cerramos conexión
    curl_close($ch);
 
    file_put_contents (get_log_filename(), "\nGCM push sent. Response:>".$response."<\n", FILE_APPEND);
    
    return $response;

}

?>
