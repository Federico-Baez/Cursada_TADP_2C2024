# frozen_string_literal: true
require_relative './error_test_fallido'
module Deberia
  def deberia(expresion)
    raise ErrorTestFallido.new("El test fallo") unless expresion.call(self)
  end

end
