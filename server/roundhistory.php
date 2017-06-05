<?php
/*   history.php: recupera los eventos de un tablero
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
    
    if (!isset($_REQUEST["roundid"])) {
        echo '{"error":"-1"}';
        return;
    }

    $roundid = $_REQUEST["roundid"];
    
    $date = isset($_REQUEST["fromdate"]) ? $_REQUEST["fromdate"] : 
                                           '2000-01-01';
    
    file_put_contents (get_log_filename(), "eventos en boardid=".$roundid."\n", FILE_APPEND);
    
    try {
        $db = nueva_conexion_db();
        
        $q = $db->prepare("SELECT * FROM getRoundHistory(:roundid,:date);");
        $q->bindParam(":roundid", $roundid, PDO::PARAM_INT);
        $q->bindParam(":date", $date, PDO::PARAM_STR);
        $q->execute();
        
        if (!isset($_REQUEST["xml"])) {
            $rr = array();
            for ($i = 0; FALSE!=($row=$q->fetch(PDO::FETCH_ASSOC)) ; ++$i) {
                $rr[] = $row;
            }
            file_put_contents (get_log_filename(), "    En json!\n", FILE_APPEND);
            print json_encode($rr);    
        }
        else {
            $xml = new SimpleXMLElement('<xml/>');

            while (FALSE!=($row=$q->fetch())) {
                $figura = $xml->addChild('roundevent');
                $figura->addChild('quien', $row['quien']);
                $figura->addChild($row['tipo']==1 ? 'codedboard' : 'que', $row['que']);
                $figura->addChild('cuando', $row['cuando']);
            }

            Header('Content-type: text/xml');
            print($xml->asXML());
       }
                
    } catch (PDOException $e) {
        echo '{"error":"-1","message":"'.$e->getMessage().'"}';
//        echo "Error".$e->getMessage();
    }

    $db = null;
    
?>
