<?php
    require_once 'funciones_y_ctes.php';

    file_put_contents (get_log_filename(), basename(__FILE__).":    ", FILE_APPEND);
?>
<html>
  <head>
    <title>Registra un nuevo juego</title>
      <meta http-equiv="Content-Type" content="text/html;charset=UTF-8" />
      <style type="text/css">
        table {
                border-style: none;
                border-collapse: collapse;
        }
        table th {
                border-width: 1px;
                padding: 1px;
                border-style: solid;
                border-color: gray;
                background-color: rgb(230, 230, 220);
        }
        table td {
                border-width: 1px;
                padding: 1px;
                border-style: solid;
                border-color: gray;
                background-color: rgb(255, 255, 240);
        }
      </style>
  
  </head>
  <body>

    <?php
       if (isset($_REQUEST['list'])) {
          echo '<h2>Registra un juego</h2>';
          if (isset($_REQUEST['message'])) {
             echo urldecode($_REQUEST['message']);
             echo '<br/>';
             echo '<br/>';
          }
          // Impresion de resultados en HTML
          echo '<table><tr><th>id</th><th>juego</th><th>descripción</th>'.
                  '<th>Mín. jugadores</th><th>Máx. jugadores</th><th>de</th></tr>';

          $db = nueva_conexion_db();

          $q = $db->query("SELECT * FROM games ORDER BY gameid DESC;");
          while(FALSE!=($row=$q->fetch())) {
              echo '<tr>';
              echo '<td>'.$row['gameid'].'</td>';
              echo '<td>'.$row['gamename'].'</td>';
              echo '<td>'.$row['gamedescription'].'</td>';
              echo '<td>'.$row['minplayers'].'</td>';
              echo '<td>'.$row['maxplayers'].'</td>';
              echo '<td>'.$row['owner'].'</td>';
              echo '</tr>';
          }
          echo '</table>';
          echo '<p><a href="registergame.php">De vuelta a registro</a></p>';
           
       } elseif (!isset($_REQUEST['gamename'])) { 
    ?>
    <h2>Registra un juego</h2>
    <form action="" method="post">
      <h4>Parámetros del juego:</h4>
      <p><a href="registergame.php?list">Lista los juegos registrados</a></p>
      (Hazlo con cuidado que aquí no comprobamos ningún parámetro)
      <table>
        <tr><td>Nombre del juego:</td><td><input type="text" name="gamename" value=""></td></tr>
        <tr><td>Descripción del juego:</td><td><input type="text" name="gamedescription" value=""></td></tr>
        <tr><td>Número mínimo de jugadores:</td><td><input type=text name="minplayers" value="2"></td></tr>
        <tr><td>Número máximo de jugadores:</td><td><input type=text name="maxplayers" value="2"></td></tr>
        <tr><td>Tu nombre:</td><td><input type=text name="owner" value=""></td></tr>
      </table>
      <input type="submit" name="" value="Enviar">
    </form>

    <?php 
      } else {
        try {
          $db = nueva_conexion_db();

          $sql = "INSERT INTO games  (gamename, gamedescription,".
                                     "minplayers, maxplayers, owner) ".
                            "VALUES  (:gamename, :gamedescription,".
                                    " :minplayers, :maxplayers, :owner)";

          $q = $db->prepare($sql);

          $q->bindParam(':gamename',        $_REQUEST['gamename'],        PDO::PARAM_STR);
          $q->bindParam(':gamedescription', $_REQUEST['gamedescription'], PDO::PARAM_STR);
          $q->bindParam(':minplayers',      $_REQUEST['minplayers'],      PDO::PARAM_INT);
          $q->bindParam(':maxplayers',      $_REQUEST['maxplayers'],      PDO::PARAM_INT);
          $q->bindParam(':owner',           $_REQUEST['owner'],           PDO::PARAM_STR);

          $q->execute();
  
          // Impresion de resultados en HTML
          //echo '<p>Juegos registrados</p>';
          
          $txt=urlencode("Juego ".$_REQUEST['gamename']." insertado correctamente en la BBDD. ".
                          "Código de juego: ".$db->lastInsertId("games_gameid_seq")); 

          header("Location: " . $_SERVER['REQUEST_URI'].'?list&message='.$txt);
          exit();
          return;
          
        }
        catch (PDOException $e) {
          print "Error!: " . $e->getMessage() . "<br/>";
        }
         
        $db = null;
      }
    ?>
  </body>
</html>

