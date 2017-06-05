<?php
/*   sendmsg.php: guarda un mensaje destinado a un username o una partida en la BBDD
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
  
    if (!isPlayerIdAccesible() || !isset($_REQUEST["msg"])) {
        echo "-1";
        file_put_contents (get_log_filename(), "Falta parametro playerid o no esta logeado", FILE_APPEND);
        return;
    }

    if (!isset($_REQUEST["to"]) && !isset($_REQUEST["toround"])) {
        echo "-1";
        file_put_contents (get_log_filename(), "Falta parametro to o toround", FILE_APPEND);
        return;
    }
    
    $toround = isset($_REQUEST["to"]) ? FALSE : TRUE;
    
    $from = getPlayerId();
    $to   = $toround ? $_REQUEST["toround"] : $_REQUEST["to"];
    $msg  = $toround ? $_REQUEST["msg"]     : substr($_REQUEST["msg"],0,139);

    file_put_contents (get_log_filename(), "de ".$from." para ".$to.": ", FILE_APPEND);
    
   // $listMobiles = array('APA91bHs0ZKifRWxHacO1RE1hb6iBkYNPLBDz-aDjfkICbQZjyE6_g9y4NoXtMZVqXOM1GbiLwbwAl-IaUrWbIzWI5c_uahibn8rn3EXS08XfSc_CffwzQJZwhS1S-2qQKa286b0PJ8B7dJ50OB4R4ly96DCOIxb92MjPZiCybW92-r5xfJFd6M');
                         

    //$msgcm = 'push push '.$msg;
   // enviaMensajePush($msgcm, $listMobiles);

    $db = nueva_conexion_db();
    
    try {

        if ($toround) {
            $sql = $db->prepare("INSERT INTO roundmessages (senderid,roundid,message)".
                                  " VALUES (:from,:to,:msg);");
        }
        else {
            $sql = $db->prepare("INSERT INTO messages (remitente,destinatario,mensaje)".
                                 " VALUES (:from,:to,:msg);");
        }

        $sql->bindParam(":from", $from, PDO::PARAM_STR);
        $sql->bindParam(":to",   $to,   PDO::PARAM_STR);
        $sql->bindParam(":msg",  $msg,  PDO::PARAM_STR);
        
        $ok = $sql->execute();
     
        if ($ok==1) {
            if ($toround) {
                file_put_contents (get_log_filename(), "toround", FILE_APPEND);
                send_gcmmessage_to_round($to,$msg,$from, MSG_TOROUND);
            }
            else {
                $q=$db->prepare("SELECT playername FROM players WHERE playerid=:playerid;");
                $q->bindParam(":playerid", $from, PDO::PARAM_STR);
                $q->execute();
                $row = $q->fetch();
                $fromname = $row['playername'];
                send_gcmmessage_to_player($to,$msg,$fromname);
            }
            file_put_contents (get_log_filename(), "OK\n", FILE_APPEND);
            echo 1;
        }
        else {
            file_put_contents (get_log_filename(), "ERROR\n", FILE_APPEND);
            echo 0;
        }
    }
    catch (PDOException $e) {
        file_put_contents (get_log_filename(), $e.$e->getMessage()."\n", FILE_APPEND);
        echo 0;
    }

    $db = null;
    
?>
