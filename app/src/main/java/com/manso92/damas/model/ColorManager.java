package com.manso92.damas.model;

import android.content.Context;

import com.manso92.damas.R;


public class ColorManager {
    public static int casillaClara(Context context){
        switch (Preferences.getColorScheme(context)) {
            case "wood":           return R.color.wood_casillaClara;
            case "byn":            return R.color.byn_casillaClara;
            case "hightcontrast":  return R.color.hightcontrast_casillaClara;
            default:               return R.color.wood_casillaClara;
        }
    }
    public static int casillaOscura(Context context){
        switch (Preferences.getColorScheme(context)) {
            case "wood":           return R.color.wood_casillaOscura;
            case "byn":            return R.color.byn_casillaOscura;
            case "hightcontrast":  return R.color.hightcontrast_casillaOscura;
            default:               return R.color.wood_casillaOscura;
        }
    }
    public static int casillaSugerida(Context context){
        switch (Preferences.getColorScheme(context)) {
            case "wood":           return R.color.wood_casillaSugerida;
            case "byn":            return R.color.byn_casillaSugerida;
            case "hightcontrast":  return R.color.hightcontrast_casillaSugerida;
            default:               return R.color.wood_casillaSugerida;
        }
    }
    public static int fichaBlanca(Context context){
        switch (Preferences.getColorScheme(context)) {
            case "wood":           return R.color.wood_fichablanca;
            case "byn":            return R.color.byn_fichablanca;
            case "hightcontrast":  return R.color.hightcontrast_fichablanca;
            default:               return R.color.wood_fichablanca;
        }
    }
    public static int fichaNega(Context context){
        switch (Preferences.getColorScheme(context)) {
            case "wood":           return R.color.wood_fichanegra;
            case "byn":            return R.color.byn_fichanegra;
            case "hightcontrast":  return R.color.hightcontrast_fichanegra;
            default:               return R.color.wood_fichanegra;
        }
    }
}
