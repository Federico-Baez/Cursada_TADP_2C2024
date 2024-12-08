# frozen_string_literal: true

class ErrorTestFallido < StandardError
  def initialize msg
    super msg
  end
end
