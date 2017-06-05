<?php
/*   addplayertoround.php: aniade un nuevo jugador a lapartida 
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
        echo "-1";
        file_put_contents (get_log_filename(), "Faltan parametros\n", FILE_APPEND);
        return;
    }

    $roundid  = $_REQUEST["roundid"];
    $playerid = getPlayerId();
    $ok = TRUE;

    $db = nueva_conexion_db();

    try {
        //$gnpout     = $db->query("SELECT getNextPlayer(".$roundid.");");
        $gnpout = $db->prepare("SELECT getNextPlayer(:roundid);");
        
        $gnpout->bindParam(":roundid", $roundid, PDO::PARAM_INT);
        
        $gnpout->execute();
        
        $gnp        = $gnpout->fetch();
        $numplayers = $gnp[0];

        if ($numplayers<=1) {
            $msg = $numplayers==1 ? "No existe el roundid=".$roundid :
                       "Num max de jugadores alcanzado para roundid=".$roundid;
            file_put_contents (get_log_filename(), "ERROR ".$msg, FILE_APPEND);
            $ok = FALSE;
        }
        else {
            // Asignamos al jugador el turno correspondiente 
            $sql = "INSERT INTO roundplayers (roundid,playerid,turn) ".
                   "VALUES (:roundid,:playerid,:numplayers);";
//                   "VALUES (".$roundid.",'".$playerid."',".$numplayers.");";
            
            $ins = $db->prepare($sql);
            
            $ins->bindParam(':roundid',    $roundid,    PDO::PARAM_INT);
            $ins->bindParam(':playerid',   $playerid,   PDO::PARAM_STR);
            $ins->bindParam(':numplayers', $numplayers, PDO::PARAM_INT);
            
            $ok = $ins->execute();
            
            //$ok = $db->exec($sql)==1;
        }
    }
    catch (PDOException $e) {
        file_put_contents (get_log_filename(), "ERROR".$e->getMessage(), FILE_APPEND);
        $ok = FALSE;
    }

    if ($ok) {
        echo $numplayers; //Devolvemos el numero de turno del jugador aniadido
    }
    else {
        echo '0';
    }

    file_put_contents (get_log_filename(), "\n", FILE_APPEND);

    $db = null;
 
?>

