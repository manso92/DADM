<?php
/*   getresults.php: recupera los resultsdos para un juego determinado
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

    if (isPlayerIdAccesible() && isset($_REQUEST["gameid"])) {
        getresults($_REQUEST["gameid"], isset($_REQUEST["groupbyuser"]));
    }
    else {
        file_put_contents (get_log_filename(), "Faltan parametros o no logeado.", FILE_APPEND);
    }
  
    file_put_contents (get_log_filename(), "\n", FILE_APPEND);
?>