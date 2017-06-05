BBDD creada en postgres. Para crear la bbdd se debe ejecutar:

createdb -U alumnodb --encoding='utf8' juegos2017

Se ha usado como usuario alumnodb. El nombre de la bbdd es 
juegos2017. Si se cambia el nombre también se debe actualizar
el fichero funciones_y_ctes.php donde se especifica este 
nombre.

Una vez creada la bbdd hay que usar el dump para crear las 
tablas con el siguiente comando:

psql -U alumnodb -f dump.sql juegos2017


