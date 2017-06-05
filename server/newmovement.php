<?php
/*   newmovement.php: mueve si le toca al jugador y si se puede jugar
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

    session_start();
 
    require_once 'funciones_y_ctes.php';

    file_put_contents (get_log_filename(), basename(__FILE__).":    ", FILE_APPEND);

    if (!isset($_REQUEST["roundid"]) || !isset($_REQUEST["codedboard"])
                                                   || !isPlayerIdAccesible()) {
        echo '{'.'"turn": "-1", "codedboard" : ""}';
        //echo "-1";
        file_put_contents (get_log_filename(), "Faltan parametros\n", FILE_APPEND);
        return;
    }

    $roundid    = $_REQUEST["roundid"];
    $playerid   = getPlayerId();
    $codedboard = $_REQUEST["codedboard"];
    $finished   = isset($_REQUEST["finished"]) ? TRUE : FALSE ;
    $overrideturn = isset($_REQUEST["next"]) ? TRUE : FALSE ;

    $ok = TRUE;

    $db = nueva_conexion_db();

    try {
        $res = $db->query("SELECT * FROM getLastCodedBoard(".$roundid.");")->fetch();
        $lastcodedboard = $res===FALSE ? '' : $res['codedboard'];

        $res = $db->query("SELECT isMyTurn(".$roundid.",'".$playerid."');")->fetch();
        $myturn = $res[0];

        $gnt = $db->query("SELECT getNextMoveNumber(".$roundid.");")->fetch();
        $nextmove = $gnt[0];
    }
    catch (PDOException $e) {
        file_put_contents (get_log_filename(), "ERROR".$e->getMessage(), FILE_APPEND);
        $ok = FALSE;
    }

    if (!$ok || $myturn <= 0) {
        file_put_contents (get_log_filename(), "ERROR: No es tu turno (".$playerid.
                     ") en partida (".$roundid.")\n", FILE_APPEND);
        echo '{'.'"turn": "0", "codedboard" : "'.$lastcodedboard.'"}';
        //echo "0:".$lastcodedboard;
        return;
    }
        
    $db->beginTransaction();
    try {
        // Aniadimos el movimiento
        $sql = "INSERT INTO roundmoves (roundid,playerid,nummove,codedboard) ".
               "VALUES (".$roundid.",'".$playerid."',".$nextmove.",'".$codedboard."');";
        $db->exec($sql);

        if ($overrideturn) {
            // sobrescribimos el turno
            $sql = "UPDATE rounds SET turn =".$_REQUEST["next"].
                  " WHERE roundid=".$roundid.";";
            $db->exec($sql);
        }
        
        if ($finished) {
            $db->query("SELECT setBoardAsFinished(".$roundid.");");
        }

        $db->commit();
    }
    catch (PDOException $e) {
        file_put_contents (get_log_filename(), "ERROR".$e->getMessage(), FILE_APPEND);
        $db->rollback();
        $ok = FALSE;
    }

    if ($ok) {
        send_gcmmessage_to_round($roundid,$codedboard,$playerid);
        echo '{'.'"turn": "1", "codedboard" : "'.$codedboard.'"}';
        //echo '1:'.$codedboard;
        file_put_contents (get_log_filename(), "".$codedboard, FILE_APPEND);
    }
    else {
        echo '{'.'"turn": "0", "codedboard" : "'.$lastcodedboard.'"}';
        //echo '0:'.$lastcodedboard;
    }

    file_put_contents (get_log_filename(), "\n", FILE_APPEND);

    $db = null;
 
?>

