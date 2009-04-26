package xtras.sql;

import java.sql.ResultSet;
import java.sql.SQLException;

/** @author Christoffer Lerno */
public interface ResultProcessor<C>
{
	boolean process(ResultSet result) throws SQLException;
	C getResult();
}
