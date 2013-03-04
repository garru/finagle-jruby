require 'test/unit'
require 'finagle'

class TestFuture < Test::Unit::TestCase
  def test_value
    future = Future.value(1)
    assert_equal 1, future.apply
  end

  def test_map
    number = 1
    future = Future.value(number)
    future2 = future.map{|x| x + 1}
    assert future2.is_a?(Future)
    assert_equal number + 1, future2.apply
  end

  def test_flat_map
    number = 1
    future = Future.value(number)
    future2 = future.flat_map{|x| Future.value(x + 1)}
    assert future2.is_a?(Future)
    assert_equal number + 1, future2.apply
  end

  def test_flat_map_raises_exception_when_block_returns_non_future
    number = 1
    future = Future.value(number)
    future2 = future.flat_map{|x| x + 1}
    assert_raise TypeError do
      future2.apply
    end
  end
end