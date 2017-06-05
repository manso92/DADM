<?php
/*   account.php: gestiona las cuentas de usuario en la DDBB
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

    $_SESSION['playerid'] = '';
    
    require_once 'funciones_y_ctes.php';

    $salida = basename(__FILE__).":     ";
    
    if (!isset($_REQUEST["playername"]) || !isset($_REQUEST["playerpassword"])) {
        $salida = $salida."faltan parámetros\n";
        file_put_contents (get_log_filename(), $salida, FILE_APPEND);
        echo "-1";
        return;
    }

    $playername = $_REQUEST["playername"];
    $password   = $_REQUEST["playerpassword"];
    $gcmregid   = isset($_REQUEST["gcmregid"]) ? $_REQUEST["gcmregid"] : '';
    $login      = isset($_REQUEST["login"]);
    
    $salida = $salida.'[url params: playername='.$playername.' & password='.$password.']';

    try {
        $db = nueva_conexion_db();

        $q = $db->prepare("SELECT playerid, password FROM players ".
                          "WHERE playername = :playername");
        
        $q->bindParam(':playername', $playername, PDO::PARAM_STR);
        
        $q->execute();
        
        if ($q->rowCount()==0 && !$login) {
            
            $ins = $db->prepare("INSERT INTO players (playerid, playername, password)".
                                " VALUES (uuid_generate_v4(), :playername, :password);");
            
            $ins->bindParam(':playername', $playername, PDO::PARAM_STR);
            $ins->bindParam(':password',   $password,   PDO::PARAM_STR);
            
            $ok = $ins->execute();
            
            if ($ok) {
                $q->execute();
                $linea = $q->fetch();
                $_SESSION['playerid'] = $linea['playerid'];
                echo $linea['playerid'];
                $salida = $salida.' Nuevo usuario creado! id='.$linea['playerid'];
            }
            else {
                $salida = $salida.' Error al insertar usuario';
                echo '-1';
            }
        }
        else if ($q->rowCount()==1 && $login) {
            $linea = $q->fetch();
            if (strcmp($linea['password'],$password)==0) {
                $_SESSION['playerid'] = $linea['playerid'];
                echo $linea['playerid'];
                $salida = $salida.' Usuario y password ok. Hacemos login para '.
                                                       'id='.$linea['playerid'];
            }
            else {
                $salida = $salida.' Usuario y password mal';
                echo '-1';
            }
            
        }
        else {
            $salida=$salida.' Usuario ya existe!';
            echo '-1';
        }
    }
    catch (PDOException $e) {
        $salida=$salida.' Excepcion DDBB';
        echo "-1";
    }

    if ($_SESSION['playerid']!='' && isset($_REQUEST["clearuserlog"])) {
        $fn=get_log_filename();
        unlink($fn);
    }

    
    if ($_SESSION['playerid']!='') {
        
        $q = $db->prepare("UPDATE players SET gcmregid = NULL".
                          " WHERE gcmregid =:gcmregid;");
        
        $q->bindParam(':gcmregid', $gcmregid, PDO::PARAM_STR);

        $ok1 = $q->execute();

        $q = $db->prepare("UPDATE players SET gcmregid =:gcmregid".
                        " WHERE playerid=:playerid;");
        
        $q->bindParam(':gcmregid', $gcmregid, PDO::PARAM_STR);
        $q->bindParam(':playerid', $_SESSION['playerid'], PDO::PARAM_STR);
            
        $ok = $q->execute();
        
        if ($ok) {
            $salida=$salida.'- GCM: (id='.$gcmregid.') aniadido a la BBDD';
        }
        else {
            $salida=$salida.'- Error al aniadir GCM a la BBDD';
        }
    }
    
    file_put_contents (get_log_filename(), $salida."\n", FILE_APPEND);
    
    $db = null;
    
?>
