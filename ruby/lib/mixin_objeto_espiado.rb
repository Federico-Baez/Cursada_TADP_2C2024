module MixinObjetoEspiado
  def utilizoMetodo(unMetodo)
     TADsPec.seUsoEsteMetodoEnEsteObjeto(self,unMetodo)
  end

  def cantidadDeVecesQueUtilizoEseMetodo(unMetodo)
    TADsPec.cuantoSeUtilizoEsteMetodo(self,unMetodo)
  end

  def parametrosUtilizadosEnElLlamado(unMetodo)
    TADsPec.cualesParametrosSeUsaronPara(self,unMetodo)
  end


end

