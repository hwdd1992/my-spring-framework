/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.transaction;

import java.sql.Connection;

import org.springframework.lang.Nullable;

/**
 * Interface that defines Spring-compliant transaction properties.
 * Based on the propagation behavior definitions analogous to EJB CMT attributes.
 * <p>该接口定义了与Spring兼容的事务属性的接口。 基于类似于EJB CMT属性的传播行为定义。
 *
 * <p>Note that isolation level and timeout settings will not get applied unless
 * an actual new transaction gets started. As only {@link #PROPAGATION_REQUIRED},
 * {@link #PROPAGATION_REQUIRES_NEW} and {@link #PROPAGATION_NESTED} can cause
 * that, it usually doesn't make sense to specify those settings in other cases.
 * Furthermore, be aware that not all transaction managers will support those
 * advanced features and thus might throw corresponding exceptions when given
 * non-default values.
 *<p>请注意，除非启动实际的新事务，否则不会应用事务的隔离级别和超时设置。 由于只有 {@link #PROPAGATION_REQUIRED}，{@link #PROPAGATION_REQUIRES_NEW}
 * 和 {@link #PROPAGATION_NESTED} 会导致这种情况，因此在其他情况下指定这些设置(指事务隔离级别和超时设置)通常是没有意义的。 此外，请注意，
 * 并非所有的事务管理器都将支持这些高级功能，因此在给定非默认值时可能会引发相应的异常。
 *
 *
 * <p>The {@link #isReadOnly() read-only flag} applies to any transaction context,
 * whether backed by an actual resource transaction or operating non-transactionally
 * at the resource level. In the latter case, the flag will only apply to managed
 * resources within the application, such as a Hibernate {@code Session}.
 * <p>只读标志适用于任何事务上下文，无论是由实际资源事务支持还是在资源级别以非事务方式进行操作。
 * 在后一种情况下，该标志仅适用于应用程序内的托管资源，例如休眠会话。
 * @author Juergen Hoeller
 * @since 08.05.2003
 * @see PlatformTransactionManager#getTransaction(TransactionDefinition)
 * @see org.springframework.transaction.support.DefaultTransactionDefinition
 * @see org.springframework.transaction.interceptor.TransactionAttribute
 */
public interface TransactionDefinition {
	/*
	文档里说的 transaction synchronization 可以理解成事务传播
	或者理解成:
	本来每个sql都有自己的事务,同步指的是同时提交同时回退
	 */

	/**
	 * Support a current transaction; create a new one if none exists.
	 * Analogous to the EJB transaction attribute of the same name.
	 * <p>支持当前事务； 如果不存在，请创建一个新的。 类似于同名的EJB事务属性。
	 *
	 * <p>This is typically the default setting of a transaction definition,
	 * and typically defines a transaction synchronization scope.
	 * <p>这通常是事务定义的默认设置，并且通常定义事务同步作用域。
	 */
	int PROPAGATION_REQUIRED = 0;

	/**
	 * Support a current transaction; execute non-transactionally if none exists.
	 * Analogous to the EJB transaction attribute of the same name.
	 * <p>支持当前事务； 如果不存在，则以非事务方式执行。 类似于同名的EJB事务属性。
	 *
	 * <p><b>NOTE:</b> For transaction managers with transaction synchronization,
	 * {@code PROPAGATION_SUPPORTS} is slightly different from no transaction
	 * at all, as it defines a transaction scope that synchronization might apply to.
	 * As a consequence, the same resources (a JDBC {@code Connection}, a
	 * Hibernate {@code Session}, etc) will be shared for the entire specified
	 * scope. Note that the exact behavior depends on the actual synchronization
	 * configuration of the transaction manager!
	 * <p><b>注意：</b>对于具有事务同步的事务管理器，{@code PROPAGATION_SUPPORTS} 与根本没有事务略有不同，因为它定义了同步可能适用的事务范围。
	 * 因此，将为整个指定范围共享相同的资源（JDBC连接，Hibernate会话等）。 请注意，确切的行为取决于事务管理器的实际同步配置！
	 *
	 * <p>In general, use {@code PROPAGATION_SUPPORTS} with care! In particular, do
	 * not rely on {@code PROPAGATION_REQUIRED} or {@code PROPAGATION_REQUIRES_NEW}
	 * <i>within</i> a {@code PROPAGATION_SUPPORTS} scope (which may lead to
	 * synchronization conflicts at runtime). If such nesting is unavoidable, make sure
	 * to configure your transaction manager appropriately (typically switching to
	 * "synchronization on actual transaction").
	 * <p> 通常，请小心使用 {@code PROPAGATION_SUPPORTS}！ 特别是，不要依赖 {@code PROPAGATION_SUPPORTS} 范围内的
	 * {@code PROPAGATION_REQUIRED} 或 {@code PROPAGATION_REQUIRES_NEW}（这可能会在运行时导致同步冲突）。
	 * 如果这种嵌套是不可避免的，请确保适当地配置您的事务管理器（通常切换到“实际事务的同步”）。
	 *
	 * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#setTransactionSynchronization
	 * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#SYNCHRONIZATION_ON_ACTUAL_TRANSACTION
	 */
	int PROPAGATION_SUPPORTS = 1;

	/**
	 * Support a current transaction; throw an exception if no current transaction
	 * exists. Analogous to the EJB transaction attribute of the same name.
	 * <p>支持当前事务,如果当前没有事务就抛异常.类似于 EJB 事务的同名属性
	 *
	 * <p>Note that transaction synchronization within a {@code PROPAGATION_MANDATORY}
	 * scope will always be driven by the surrounding transaction.
	 * <p>请注意，{@code PROPAGATION_MANDATORY} 范围内的事务同步将始终由周围的事务驱动。
	 */
	int PROPAGATION_MANDATORY = 2;

	/**
	 * Create a new transaction, suspending the current transaction if one exists.
	 * Analogous to the EJB transaction attribute of the same name.
	 * <p>创建一个新的事务,如果当前已经有事务存在,就挂起已存在的事务.类似于 EJB 事务的同名属性.
	 *
	 * <p><b>NOTE:</b> Actual transaction suspension will not work out-of-the-box
	 * on all transaction managers. This in particular applies to
	 * {@link org.springframework.transaction.jta.JtaTransactionManager},
	 * which requires the {@code javax.transaction.TransactionManager} to be
	 * made available it to it (which is server-specific in standard Java EE).
	 * <p><b>注意:</b>实际的事务挂起不会在所有事务管理器中开箱即用。 这尤其适用于 {@link org.springframework.transaction.jta.JtaTransactionManager},
	 * 它要求 {@code javax.transaction.TransactionManager} 对其可用（在标准Java EE中服务器特定）。
	 *
	 * <p>A {@code PROPAGATION_REQUIRES_NEW} scope always defines its own
	 * transaction synchronizations. Existing synchronizations will be suspended
	 * and resumed appropriately.
	 * <p> {@code PROPAGATION_REQUIRES_NEW} 范围始终定义自己的事务同步。 现有同步将暂停并在适当时候恢复。
	 * @see org.springframework.transaction.jta.JtaTransactionManager#setTransactionManager
	 */
	int PROPAGATION_REQUIRES_NEW = 3;

	/**
	 * Do not support a current transaction; rather always execute non-transactionally.
	 * Analogous to the EJB transaction attribute of the same name.
	 * <p>不支持当前事务； 而是始终以非事务方式执行。 类似于同名的EJB事务属性。
	 *
	 * <p><b>NOTE:</b> Actual transaction suspension will not work out-of-the-box
	 * on all transaction managers. This in particular applies to
	 * {@link org.springframework.transaction.jta.JtaTransactionManager},
	 * which requires the {@code javax.transaction.TransactionManager} to be
	 * made available it to it (which is server-specific in standard Java EE).
	 * <p><b>注意:</b>实际的事务挂起不会在所有事务管理器中开箱即用。 这尤其适用于 {@link org.springframework.transaction.jta.JtaTransactionManager},
	 * 它要求 {@code javax.transaction.TransactionManager} 对其可用（在标准Java EE中服务器特定）。
	 *
	 * <p>Note that transaction synchronization is <i>not</i> available within a
	 * {@code PROPAGATION_NOT_SUPPORTED} scope. Existing synchronizations
	 * will be suspended and resumed appropriately.
	 * <p>注意事务同步不适用于{@code PROPAGATION_NOT_SUPPORTED}范围,现有同步将暂停并在适当时候恢复。
	 *
	 * @see org.springframework.transaction.jta.JtaTransactionManager#setTransactionManager
	 */
	int PROPAGATION_NOT_SUPPORTED = 4;

	/**
	 * Do not support a current transaction; throw an exception if a current transaction
	 * exists. Analogous to the EJB transaction attribute of the same name.
	 * <p>不支持当前事务,如果当前存在事务就会抛异常.类似于 EJB 同名事务
	 *
	 * <p>Note that transaction synchronization is <i>not</i> available within a
	 * {@code PROPAGATION_NEVER} scope.
	 * <p> 注意事务同步不适用与 {@code PROPAGATION_NEVER} 范围
	 */
	int PROPAGATION_NEVER = 5;

	/**
	 * Execute within a nested transaction if a current transaction exists,
	 * behave like {@link #PROPAGATION_REQUIRED} otherwise. There is no
	 * analogous feature in EJB.
	 * <p>如果当前事务存在，则在嵌套事务中执行，否则，行为类似于 {@link #PROPAGATION_REQUIRED}。 EJB中没有类似的功能。
	 *
	 * <p><b>NOTE:</b> Actual creation of a nested transaction will only work on
	 * specific transaction managers. Out of the box, this only applies to the JDBC
	 * {@link org.springframework.jdbc.datasource.DataSourceTransactionManager}
	 * when working on a JDBC 3.0 driver. Some JTA providers might support
	 * nested transactions as well.
	 * <p><b>注意:</b>实际创建嵌套事务将仅在特定事务管理器上起作用。 开箱即用，仅在使用JDBC 3.0驱动程序时才适用于
	 * JDBC {@link org.springframework.jdbc.datasource.DataSourceTransactionManager}。 一些JTA提供程序可能也支持嵌套事务。
	 * @see org.springframework.jdbc.datasource.DataSourceTransactionManager
	 */
	int PROPAGATION_NESTED = 6;


	/**
	 * Use the default isolation level of the underlying datastore.
	 * All other levels correspond to the JDBC isolation levels.
	 * <p>使用底层数据存储的默认隔离级别。 所有其他级别对应于JDBC隔离级别。
	 * @see java.sql.Connection
	 */
	int ISOLATION_DEFAULT = -1;

	/**
	 * Indicates that dirty reads, non-repeatable reads and phantom reads
	 * can occur.
	 * <p>表示可能发生脏读，不可重复读和幻像读。
	 *
	 * <p>This level allows a row changed by one transaction to be read by another
	 * transaction before any changes in that row have been committed (a "dirty read").
	 * If any of the changes are rolled back, the second transaction will have
	 * retrieved an invalid row.
	 * <p>事务A更改了某行数据,当还没有提交,这个更改就可以被事务B读取.当事务A发生回滚,那么事务B读到的数据就变成无效数据
	 * @see java.sql.Connection#TRANSACTION_READ_UNCOMMITTED
	 */
	int ISOLATION_READ_UNCOMMITTED = Connection.TRANSACTION_READ_UNCOMMITTED;

	/**
	 * Indicates that dirty reads are prevented; non-repeatable reads and
	 * phantom reads can occur.
	 * <p>不会发生脏读,但是可能发生不可重复读和幻像读。
	 *
	 * <p>This level only prohibits a transaction from reading a row
	 * with uncommitted changes in it.
	 * <p>此级别只会读取其它事务已经提交的更改
	 * @see java.sql.Connection#TRANSACTION_READ_COMMITTED
	 */
	int ISOLATION_READ_COMMITTED = Connection.TRANSACTION_READ_COMMITTED;

	/**
	 * Indicates that dirty reads and non-repeatable reads are prevented;
	 * phantom reads can occur.
	 * <p>不会发生脏读和不可重复读,但是可能发生幻像读。
	 * <p>This level prohibits a transaction from reading a row with uncommitted changes
	 * in it, and it also prohibits the situation where one transaction reads a row,
	 * a second transaction alters the row, and the first transaction re-reads the row,
	 * getting different values the second time (a "non-repeatable read").
	 * @see java.sql.Connection#TRANSACTION_REPEATABLE_READ
	 */
	int ISOLATION_REPEATABLE_READ = Connection.TRANSACTION_REPEATABLE_READ;

	/**
	 * Indicates that dirty reads, non-repeatable reads and phantom reads
	 * are prevented.
	 * <p>不会发生脏读,不可重复读和幻像读。
	 * <p>This level includes the prohibitions in {@link #ISOLATION_REPEATABLE_READ}
	 * and further prohibits the situation where one transaction reads all rows that
	 * satisfy a {@code WHERE} condition, a second transaction inserts a row
	 * that satisfies that {@code WHERE} condition, and the first transaction
	 * re-reads for the same condition, retrieving the additional "phantom" row
	 * in the second read.
	 * <p>这个级别的事务除了符合{@link #ISOLATION_REPEATABLE_READ}的条件外,还进一步禁止了一种情况:
	 * 当事务A 根据"条件"读取了所有符合的数据,这个时候事务B又插入了一条符合"条件"的数据.然后事务A再此读取的时候
	 * 会读入事务B插入的数据.(发生了幻象读)
	 * @see java.sql.Connection#TRANSACTION_SERIALIZABLE
	 */
	int ISOLATION_SERIALIZABLE = Connection.TRANSACTION_SERIALIZABLE;


	/**
	 * Use the default timeout of the underlying transaction system,
	 * or none if timeouts are not supported.
	 * <p>使用底层数据库默认的超时时间.如果不支持超时，则不使用默认超时。
	 */
	int TIMEOUT_DEFAULT = -1;


	/**
	 * Return the propagation behavior.
	 * <p>Must return one of the {@code PROPAGATION_XXX} constants
	 * defined on {@link TransactionDefinition this interface}.
	 * @return the propagation behavior
	 * @see #PROPAGATION_REQUIRED
	 * @see org.springframework.transaction.support.TransactionSynchronizationManager#isActualTransactionActive()
	 */
	int getPropagationBehavior();

	/**
	 * Return the isolation level.
	 * <p>Must return one of the {@code ISOLATION_XXX} constants defined on
	 * {@link TransactionDefinition this interface}. Those constants are designed
	 * to match the values of the same constants on {@link java.sql.Connection}.
	 *<p>必须返回在此接口上定义的 {@code ISOLATION_XXX} 常量之一。 这些常量旨在与 {@link java.sql.Connection} 上相同常量的值匹配。
	 *
	 * <p>Exclusively designed for use with {@link #PROPAGATION_REQUIRED} or
	 * {@link #PROPAGATION_REQUIRES_NEW} since it only applies to newly started
	 * transactions. Consider switching the "validateExistingTransactions" flag to
	 * "true" on your transaction manager if you'd like isolation level declarations
	 * to get rejected when participating in an existing transaction with a different
	 * isolation level.
	 * <p>专为与 {@link #PROPAGATION_REQUIRED} 或 {@link #PROPAGATION_REQUIRES_NEW} 一起使用而设计，因为它仅适用于新启动的事务。
	 * 如果希望在参与具有不同隔离级别的现有事务时拒绝隔离级别声明，请考虑在事务管理器上将“ validateExistingTransactions”标志切换为“ true”。
	 *
	 * <p>Note that a transaction manager that does not support custom isolation levels
	 * will throw an exception when given any other level than {@link #ISOLATION_DEFAULT}.
	 * <p>请注意，不支持自定义隔离级别的事务管理器在给定除 {@link #ISOLATION_DEFAULT} 之外的任何其他级别时，将引发异常。
	 * @return the isolation level
	 * @see #ISOLATION_DEFAULT
	 * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#setValidateExistingTransaction
	 */
	int getIsolationLevel();

	/**
	 * Return the transaction timeout.
	 * <p>Must return a number of seconds, or {@link #TIMEOUT_DEFAULT}.
	 * <p>必须返回数字,单位是秒或者返回{@link #TIMEOUT_DEFAULT}
	 *
	 * <p>Exclusively designed for use with {@link #PROPAGATION_REQUIRED} or
	 * {@link #PROPAGATION_REQUIRES_NEW} since it only applies to newly started
	 * transactions.
	 * <p>专为与 {@link #PROPAGATION_REQUIRED} 或 {@link #PROPAGATION_REQUIRES_NEW} 一起使用而设计，因为它仅适用于新启动的事务。
	 *
	 * <p>Note that a transaction manager that does not support timeouts will throw
	 * an exception when given any other timeout than {@link #TIMEOUT_DEFAULT}.
	 * @return the transaction timeout
	 */
	int getTimeout();

	/**
	 * Return whether to optimize as a read-only transaction.
	 * <p>返回是否优化为只读事务。
	 *
	 * <p>The read-only flag applies to any transaction context, whether backed
	 * by an actual resource transaction ({@link #PROPAGATION_REQUIRED}/
	 * {@link #PROPAGATION_REQUIRES_NEW}) or operating non-transactionally at
	 * the resource level ({@link #PROPAGATION_SUPPORTS}). In the latter case,
	 * the flag will only apply to managed resources within the application,
	 * such as a Hibernate {@code Session}.
	 * <p>只读标志适用于任何事务上下文，无论是由实际资源事务（{@link #PROPAGATION_REQUIRED} / {@link #PROPAGATION_REQUIRES_NEW}）
	 * 支持 还是在资源级别（{@link #PROPAGATION_SUPPORTS}）非事务操作。 在后一种情况下，该标志仅适用于应用程序内的托管资源，例如休眠会话。
	 *
	 * <p>This just serves as a hint for the actual transaction subsystem;
	 * it will <i>not necessarily</i> cause failure of write access attempts.
	 * A transaction manager which cannot interpret the read-only hint will
	 * <i>not</i> throw an exception when asked for a read-only transaction.
	 * <p>这只是对实际事务子系统的提示。 它不一定会导致写访问尝试失败。 当请求只读事务时，无法解释只读提示的事务管理器不会引发异常。
	 * @return {@code true} if the transaction is to be optimized as read-only
	 * @see org.springframework.transaction.support.TransactionSynchronization#beforeCommit(boolean)
	 * @see org.springframework.transaction.support.TransactionSynchronizationManager#isCurrentTransactionReadOnly()
	 */
	boolean isReadOnly();

	/**
	 * Return the name of this transaction. Can be {@code null}.
	 * <p>返回当前事务的名字.可以为空
	 *
	 * <p>This will be used as the transaction name to be shown in a
	 * transaction monitor, if applicable (for example, WebLogic's).
	 *
	 * <p>In case of Spring's declarative transactions, the exposed name will be
	 * the {@code fully-qualified class name + "." + method name} (by default).
	 * @return the name of this transaction
	 * @see org.springframework.transaction.interceptor.TransactionAspectSupport
	 * @see org.springframework.transaction.support.TransactionSynchronizationManager#getCurrentTransactionName()
	 */
	@Nullable
	String getName();

}
