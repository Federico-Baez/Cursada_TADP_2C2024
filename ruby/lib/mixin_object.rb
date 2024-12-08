module MixinObject
  def method_missing(metodo, *args, &block)
    if metodo.start_with?("ser_")
      nombre_procesado = (metodo.to_s.delete_prefix("ser_") << "?").to_sym
      OperadorDinamico.new(nombre_procesado, args)
    elsif metodo.start_with?("tener_")
      nombre_procesado = (metodo.to_s.delete_prefix("tener_")).to_sym
      OperadorDinamico.new(nombre_procesado, args)
    else
      super
    end
  end

  def respond_to_missing?(metodo, priv = false)
    super || metodo.start_with?("ser_") || metodo.start_with?("tener_")
  end

end
