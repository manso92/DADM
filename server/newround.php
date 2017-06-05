<?php
/*   newround.php: aniade una nueva partida a la BBDD para el juego indicado y
 *                 aniade al jugador con el primer turno
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

    if (!isset($_REQUEST["gameid"]) || !isPlayerIdAccesible()) {
        echo "-1";
        file_put_contents (get_log_filename(), "Faltan parametros o no logeado\n", FILE_APPEND);
        return;
    }

    $gameid      = $_REQUEST["gameid"];
    $playerid    = getPlayerId();
    $codedboard  = isset($_REQUEST["codedboard"]) ? $_REQUEST["codedboard"] : "" ;
 
    $ok = 1;

    $db = nueva_conexion_db();

    $db->beginTransaction();
    try {
 
        // Aniadimos la partida
        $sql = "INSERT INTO rounds (gameid) VALUES (".$gameid.");";
        $out0 = $db->exec($sql);
        $roundid = $db->lastInsertId("rounds_roundid_seq"); 
 
        // Asignamos al jugador como primero de la partida
        $sql = "INSERT INTO roundplayers (roundid,playerid,turn) ".
                                  "VALUES (".$roundid.",'".$playerid."',"."1);";
        $out1 = $db->exec($sql);
 
        // Aniadimos la cadena de inicializacion como primer movimiento del
        // juego
        $sql = "INSERT INTO roundmoves (roundid,playerid,codedboard) ".
               "VALUES (".$roundid.",'".$playerid."','".$codedboard."');";
        $out2 = $db->exec($sql);
 
        if ($out0 && $out1 && $out2) {
            $db->commit();
        }
        else  {
            file_put_contents (get_log_filename(), "ERROR valores no insertados", FILE_APPEND);
            $db->rollback();
            $ok = 0;
        }
    }
    catch (PDOException $e) {
        file_put_contents (get_log_filename(), "ERROR".$e->getMessage(), FILE_APPEND);
        $db->rollback();
        $ok = 0;
    }

    if ($ok==1) {
        file_put_contents (get_log_filename(), "OK    gameid=".$gameid." codedboard=".
                            $inboard." playerid=".$playerid, FILE_APPEND);
        echo $roundid;
    }
    else {
        echo -1;
    }

    file_put_contents (get_log_filename(), "\n", FILE_APPEND);

    $db = null;
 
?>

