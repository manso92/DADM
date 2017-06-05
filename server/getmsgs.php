<?php
/*   getmsgs.php: recupera los mensajes destinados a un playerid o roundid
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
    
    if (!isPlayerIdAccesible() && !isset($_REQUEST["roundid"])) {
        echo '{"error":"-1"}';
        return;
    }

    $toround = isset($_REQUEST["roundid"]);
    
    $to = isset($_REQUEST["roundid"]) ? $_REQUEST["roundid"] : getPlayerId();
    
    $conversation = isset($_REQUEST["playername"]);
    
    $conversations = isset($_REQUEST["conversations"]);
    
    file_put_contents (get_log_filename(), "msgs para".$to."\n", FILE_APPEND);

    try {

        $db = nueva_conexion_db();

        if ($toround) {
            $date = isset($_REQUEST["fromdate"]) ? $_REQUEST["fromdate"] : 
                                                   '2000-01-01';
            $q = $db->query("SELECT * FROM getRoundMsgs('".$to."','".$date."')");
        }
        elseif ($conversation) {
            $other = $_REQUEST["playername"];
            $q = $db->query("SELECT * FROM getConversation('".$to."','".$other."')");
        }
        elseif ($conversations) {
            $q = $db->query("SELECT * FROM getConversationUsers('".$to."')");
        }
        else {
            $q = $db->query("SELECT * FROM getMsgs('".$to."')");
        }
 
        if (!isset($_REQUEST["xml"])) {
            $rr = array();
            while ($row=$q->fetch(PDO::FETCH_ASSOC)) {
                $rr[] = $row;
            }

            print json_encode($rr);
        }
        else {
            $xml = new SimpleXMLElement('<xml/>');

            while( FALSE!=($row=$q->fetch()) ) {
                $figura = $xml->addChild('message');
                $figura->addChild('from', $row['playername']);
                $figura->addChild('body', array_key_exists('message', row) ? $row['message'] : '');
                $figura->addChild('date', $row['msgdate']);
            }

            Header('Content-type: text/xml');
            print($xml->asXML());
        }
        
        if (FALSE==$toround && isset($_REQUEST["markasread"])) {
            $sql = "UPDATE messages SET entregado=true FROM players AS des ".
                   "WHERE  messages.destinatario = des.playername AND".
                        "  des.playerid='".$to."';";
            $db->exec($sql);

        }
    } catch (PDOException $e) {
        echo '{"error" : "-2"}';
        echo $e;
    }

    $db = null;
    
?>
