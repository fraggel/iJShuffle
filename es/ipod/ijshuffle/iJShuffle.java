package es.ipod.ijshuffle;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.xerces.impl.dv.util.HexBin;


public class iJShuffle {

	private static final int NUMLINEAS = 558;
	private static int numeroArchivos=1;
	private static int numArchivos=1;
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		String rutaPrincipal = System.getProperty("user.dir");
		rutaPrincipal=rutaPrincipal.replace("\\","");
		String cabeceraPorFichero="01000000022E5AA501000000000000000000000000000000000000000064000001000200";
		byte[] fin=HexBin.decode("010000");
		FileOutputStream ficheroLog=new FileOutputStream(rutaPrincipal+"/iJShuffle.log");
		try {
			ficheroLog.write(("Ficheros escritos en el archivo iTunesSD\n\n\n").getBytes());
			
			//Obtengo todos los archivos y carpetas del nivel principal y ordeno la lista
			File directorioMusica=new File(rutaPrincipal+"/");
			File[] files = ordenarLista(directorioMusica.listFiles());

			File fichero1=new File(rutaPrincipal+"/iPod_Control/iTunes/iTunesStats");
			File fichero2=new File(rutaPrincipal+"/iPod_Control/iTunes/iTunesPState");
			File fichero3=new File(rutaPrincipal+"/iPod_Control/iTunes/iTunesShuffle");
			if(fichero1.exists()){
				ficheroLog.write((fichero1.getName()+" restaurando----- OK\n").getBytes());
				fichero1.delete();
			}else{
				ficheroLog.write((fichero1.getName()+" restaurando----- NO ES NECESARIO\n").getBytes());
			}
			if(fichero2.exists()){
				ficheroLog.write((fichero2.getName()+" restaurando----- OK\n").getBytes());
				fichero2.delete();
			}else{
				ficheroLog.write((fichero2.getName()+" restaurando----- NO ES NECESARIO\n").getBytes());
			}
			if(fichero3.exists()){
				ficheroLog.write((fichero3.getName()+" restaurando----- OK\n").getBytes());
				fichero3.delete();
			}else{
				ficheroLog.write((fichero3.getName()+" restaurando----- NO ES NECESARIO\n").getBytes());
			}
			ficheroLog.write(("\n\n").getBytes());
			
			ficheroLog.write(("CREANDO LISTA DE REPRODUCCION\n\n").getBytes());
			File fichero=new File(rutaPrincipal+"/iPod_Control/iTunes/iTunesSD");
			if(fichero.exists()){
				fichero.renameTo(new File(rutaPrincipal+"/iPod_Control/iTunes/iTunesSD.old"));
				ficheroLog.write(("Se ha creado una copia de seguridad del archivo anterior en "+rutaPrincipal+"/iPod_Control/iTunes/iTunesSD.old\n\n").getBytes());
			}
			ficheroLog.write(("Se han encontrado \'"+String.valueOf(contarCanciones(files, numArchivos)-1)+"\' archivos \n\n").getBytes());
			
			OutputStream baos=new BufferedOutputStream(new FileOutputStream(rutaPrincipal+"/iPod_Control/iTunes/iTunesSD"));

			ArrayList<byte[]> listadeBytes=new ArrayList<byte[]>();
			listadeBytes=examinar(files,rutaPrincipal,cabeceraPorFichero,ficheroLog,listadeBytes);
			
			String numeroEnHex=Integer.toHexString(listadeBytes.size());
			if(numeroEnHex.length()<6){
				String renumero="";
				int num=6-numeroEnHex.length();
				for (int i = 0; i < num; i++) {
					renumero+="0";				
				}
				renumero+=numeroEnHex;
				numeroEnHex=renumero;
			}
			baos.write(HexBin.decode(numeroEnHex+"01060000001200000000000000000000"));
			for (Iterator iter = listadeBytes.iterator(); iter.hasNext();) {
				byte[] element = (byte[]) iter.next();
				baos.write(element);
				
			}
			baos.write(fin);
			baos.flush();
			baos.close();
			ficheroLog.write(("\n\nTerminado con exito").getBytes());
			System.out.println("Terminado con exito");
			ficheroLog.flush();
			ficheroLog.close();
		} catch (Exception e) {
			System.out.println("Ha ocurrido un problema, por favor revise el log");
			ficheroLog.close();
			FileOutputStream ficheroLog2=new FileOutputStream(rutaPrincipal+"/iJShuffle.log");
			StackTraceElement[] stackTrace = e.getStackTrace();
			ficheroLog2.write((e.getMessage()+"\n").getBytes());
			for (int i = 0; i < stackTrace.length; i++) {
				StackTraceElement element = stackTrace[i];
				ficheroLog2.write(("\t"+element.toString()+"\n").getBytes());
			}
			ficheroLog2.flush();
			ficheroLog2.close();
			
		}
	}
	private static int contarCanciones(File[] files,int numArchivos) throws Exception{
		for (int i = 0; i < files.length; i++) {
			File b = files[i];
			if(b.isDirectory()){
				numArchivos=contarCanciones(ordenarLista(b.listFiles()),numArchivos);
			}else{
				if(comprobarExtensiones(b.getAbsolutePath())){
					numArchivos++;
				}
			}
		}
		return numArchivos;
	}
	private static ArrayList<byte[]> escribirCancioines(File subNivel, String rutaPrincipal, String cabeceraPorFichero, FileOutputStream ficheroLog, ArrayList<byte[]> listadeBytes) throws Exception {
				
				String nombreCancion=subNivel.getAbsolutePath();
				String nombreCancionTemp="";
				if(comprobarExtensiones(nombreCancion)){
					
					nombreCancionTemp=cambiarCaracteresEspeciales(nombreCancion);
					if(!nombreCancion.equals(nombreCancionTemp)){
						ficheroLog.write(("Renombrado el archivo \'"+numeroArchivos+"\' por contener caracteres especiales, se sustituyeron por el signo \'_\'\n").getBytes());
						subNivel.renameTo(new File(nombreCancionTemp));
					}
					subNivel=new File(nombreCancionTemp);
					String rutaSubNivel=subNivel.getAbsolutePath();
					
					while(rutaSubNivel.indexOf('\\')!=-1){
						rutaSubNivel=rutaSubNivel.replace('\\', '/');
					}
					rutaSubNivel=rutaSubNivel.replaceFirst(rutaPrincipal, "");
					
					ficheroLog.write((String.valueOf(numeroArchivos)+" "+rutaSubNivel+"\n").getBytes());
					if(numeroArchivos==1){
						cabeceraPorFichero=cabeceraPorFichero.substring(8);
					}
					byte[] cabeceraFichero=HexBin.decode(cabeceraPorFichero);
					byte[] nomFichero=rutaSubNivel.getBytes();
					byte[] ficheroTmp2=new byte[cabeceraFichero.length];
					byte[] ficheroTmp;
					if(numeroArchivos==1){
						ficheroTmp=new byte[NUMLINEAS-4];
					}else{
						ficheroTmp=new byte[NUMLINEAS];
					}
					for (int i = 0; i < cabeceraFichero.length; i++) {
						byte b = cabeceraFichero[i];
						ficheroTmp[i]=b;
						
					}
					int r=ficheroTmp2.length;
					
					for (int i = 0; i < nomFichero.length; i++) {
						ficheroTmp[r]=nomFichero[i];
						if(i!=nomFichero.length-1){
							ficheroTmp[r+1]=00;
							r=r+2;
						}
					}
					
					//System.out.println(ficheroTmp+"\n\n");
					numeroArchivos++;
					listadeBytes.add(ficheroTmp);
				}
	return listadeBytes;	
	}
	private static boolean comprobarExtensiones(String rutaFichero){
		String[] extensiones={"MP3","WAV","WMV","WMA","mp3","wav","wmv","wma"};
		boolean valida=false;
		for (int i = 0; i < extensiones.length; i++) {
			String array_element = extensiones[i];
			if(rutaFichero.indexOf(array_element)!=-1){
				valida=true;
			}
		}
		return valida;
		
	}
	private static String cambiarCaracteresEspeciales(String rutaFichero) throws Exception{
		byte[] bytes = rutaFichero.getBytes();
		byte[] bytes2 = rutaFichero.getBytes();

			for (int r = 0; r < bytes.length; r++) {
				byte b = bytes[r];
				if((b<38 || b>122) && b!=32){
					bytes2[r]=95;
				}		
			}

		ByteArrayOutputStream bo=new ByteArrayOutputStream();
		bo.write(bytes2);
		
		rutaFichero=bo.toString();
		return rutaFichero;
		
	}
	private static File[] ordenarLista(File[] lista){
	        File tmp;
	        for(int k = 0; k < lista.length; k++)
	            for(int i = 0; i < lista.length - 1; i++)
	            {
	                if ( lista[i].compareTo(lista[i+1]) > 0)
	                {
	                    tmp = lista[i];
	                    lista[i] = lista[i+1];
	                    lista[i+1] = tmp;    
	                }
	            }
	        
		 return lista;
		
	}
	private static ArrayList<byte[]> examinar(File[] files, String rutaPrincipal, String cabeceraPorFichero, FileOutputStream ficheroLog,ArrayList<byte[]> listadeBytes) throws Exception {
		
		for (int i = 0; i < files.length; i++) {
			File subNivel = files[i];
			if(subNivel.isDirectory()){
				String nombreCarpetaTemp=cambiarCaracteresEspeciales(subNivel.getAbsolutePath());
				if(!nombreCarpetaTemp.equals(subNivel.getAbsolutePath())){
					ficheroLog.write(("Renombrada la carpeta \'"+subNivel.getName()+"\' por contener caracteres especiales, se sustituyeron por el signo \'_\'\n").getBytes());
					File renamed=new File(nombreCarpetaTemp);
					subNivel.renameTo(renamed);
					subNivel=renamed;
				}
				listadeBytes=examinar(ordenarLista(subNivel.listFiles()),rutaPrincipal,cabeceraPorFichero,ficheroLog,listadeBytes);
			}
		}
		for (int i = 0; i < files.length; i++) {
			File subNivel = files[i];
			if(!subNivel.isDirectory()){
			
				listadeBytes=escribirCancioines(subNivel,rutaPrincipal,cabeceraPorFichero,ficheroLog,listadeBytes);
			}
			
		}
		return listadeBytes;
	}
}
