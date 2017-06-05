<?php
/*   addresult.php: aniade un nuevo jugador a lapartida 
 * 
 *   Copyright (C) 2016  Gonzalo Martinez Munoz, David Arroyo, Alejandro Sierra
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

    if (!isPlayerIdAccesible() || !isset($_REQUEST["gameid"])) {
        echo "-1";
        file_put_contents (get_log_filename(), "Faltan parametros: playerid o gameid\n", FILE_APPEND);
        return;
    }

    if ( !(isset($_REQUEST["roundtime"]) || isset($_REQUEST["points"]) 
            || isset($_REQUEST["otherinfo"])) ) {
        echo "-1";
        file_put_contents (get_log_filename(), "Falta por especificar los".
                            " puntos o el tiempo u otra info\n", FILE_APPEND);
        return;
    }
    
    $playerid  = getPlayerId();
    $gameid    = $_REQUEST["gameid"];
    $roundtime = isset($_REQUEST["roundtime"]) ? $_REQUEST["roundtime"] : 0;
    $points    = isset($_REQUEST["points"])    ? $_REQUEST["points"]    : 0;
    $otherinfo = isset($_REQUEST["otherinfo"]) ? $_REQUEST["otherinfo"] : '';
    
    $ok = TRUE;

    $db = nueva_conexion_db();

    try {
        // Asignamos al jugador el turno correspondiente 
        $sql = "INSERT INTO results (playerid,roundtime,points,otherinfo,gameid) ".
               "VALUES (:playerid,:roundtime,:points,:otherinfo,:gameid);";
        
        $ins = $db->prepare($sql);

        $ins->bindParam(':playerid',  $playerid,  PDO::PARAM_STR);
        $ins->bindParam(':roundtime', $roundtime, PDO::PARAM_INT);
        $ins->bindParam(':points',    $points,    PDO::PARAM_INT);
        $ins->bindParam(':otherinfo', $otherinfo, PDO::PARAM_STR);
        $ins->bindParam(':gameid',    $gameid,    PDO::PARAM_INT);

        $ok = $ins->execute();
        
    }
    catch (PDOException $e) {
        file_put_contents (get_log_filename(), "ERROR".$e->getMessage().$sql, FILE_APPEND);
        $ok = FALSE;
    }

    if ($ok) {
        echo '1'; //Devolvemos ok
    }
    else {
        echo '0';
    }

    file_put_contents (get_log_filename(), "\n", FILE_APPEND);

    $db = null;
 
?>
