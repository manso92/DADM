package es.uam.eps.dadm.view.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewPagerAdapter manejará las páginas que tendrá nuestro viewpage, permitiento añadir nuevos
 * fragments a nuestro contenedor y poder mostrar las partidas
 *
 * @author Pablo Manso
 * @version 11/05/2017
 */
public class ViewPagerAdapter extends FragmentPagerAdapter {

    /**
     * Lista de fragmentos que tendrá nuestro viewpager
     */
    private final List<Fragment> mFragmentList = new ArrayList<>();

    /**
     * Titulos de los fragmentos que se verá en el tab
     */
    private final List<String> mFragmentTitleList = new ArrayList<>();

    /**
     * Construgtor de la clase
     *
     * @param manager FragmentManager de los fragments que tendrá nuestro viewpger
     */
    public ViewPagerAdapter(FragmentManager manager) {
        super(manager);
    }

    /**
     * Devuelve el fragmento que esta en una posición determinada
     *
     * @param position Posición del fragmento que queremos rescatar
     * @return Fragmento rescatado de la posición indicada
     */
    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    /**
     * Devuelve el titulo del fragmento que esta en una posición determinada
     *
     * @param position Posición del fragmento del que queremos obtener el título
     * @return Título del fragmento que está en la posición indicada
     */
    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentTitleList.get(position);
    }

    /**
     * Indica el número de páginas que tendrá nuestro ViewPager
     *
     * @return Número de páginas
     */
    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    /**
     * Añade un nuevo fragmento a nuestro ViewPager
     *
     * @param fragment Fragmento a añadir
     * @param title    Título del fragmento
     */
    public void addFragment(Fragment fragment, String title) {
        // Añadimos el fragmento y el título a las listas donde se almacenan
        mFragmentList.add(fragment);
        mFragmentTitleList.add(title);
        // Indicamos a la clase superior que han cambiado las páginas
        this.notifyDataSetChanged();
    }
}