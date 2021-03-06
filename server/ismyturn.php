<?php
/*   ismyturn.php: indica si le toca al jugador
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
    
    if (!isset($_REQUEST["roundid"]) || !isPlayerIdAccesible()) {
        echo '{'.'"turn": "-1", "codedboard" : ""}';
        file_put_contents (get_log_filename(), "Faltan parametros\n", FILE_APPEND);
        return;
    }

    $roundid  = $_REQUEST["roundid"];
    $playerid = getPlayerId();
    $ok = 1;

    $db = nueva_conexion_db();

    try {
        $queryres       = $db->prepare("SELECT * FROM getLastCodedBoard(:roundid);");//$db->query("SELECT * FROM getLastCodedBoard(".$roundid.");");
        $queryres->bindParam(":roundid", $roundid, PDO::PARAM_INT);
        $queryres->execute();

        $res            = $queryres->fetch();
        $lastcodedboard = $res==FALSE ? '' : $res['codedboard'];

        $queryres = $db->prepare("select isMyTurn(:roundid,:playerid);");//$db->query("select isMyTurn(".$roundid.",'".$playerid."');");
        $queryres->bindParam(":roundid",  $roundid,  PDO::PARAM_INT);
        $queryres->bindParam(":playerid", $playerid, PDO::PARAM_STR);
        $queryres->execute();
        
        $res      = $queryres->fetch();
        $myturn   = $res[0];

        $ok = $myturn <= 0 ? 0 : 1;
    }
    catch (PDOException $e) {
        file_put_contents (get_log_filename(), "ERROR".$e->getMessage(), FILE_APPEND);
        $ok = 0;
    }

    if ($ok==0) {
        file_put_contents (get_log_filename(), "ERROR: No es tu turno (".$playerid.
                         ") en partida (".$roundid.")\n", FILE_APPEND);
    }

    echo '{'.'"turn": "'.$myturn.'", "codedboard" : "'.$lastcodedboard.'"}';

    file_put_contents (get_log_filename(), $lastcodedboard."\n", FILE_APPEND);

    $db = null;
 
?>

