package xtras.sql;

/** @author Christoffer Lerno */
public interface FakeResultGenerator
{
	Object[] createResult(int row, Object[] arguments);
}
