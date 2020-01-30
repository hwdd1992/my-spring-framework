/*
 * Copyright 2002-2014 the original author or authors.
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

/**
 * Interface that specifies an API to programmatically manage transaction
 * savepoints in a generic fashion. Extended by TransactionStatus to
 * expose savepoint management functionality for a specific transaction.
 * <p>该接口指定用于以通用方式通过编程方式管理事务保存点的API。 由TransactionStatus扩展，以公开特定事务的保存点管理功能。
 *
 * <p>Note that savepoints can only work within an active transaction.
 * Just use this programmatic savepoint handling for advanced needs;
 * else, a subtransaction with PROPAGATION_NESTED is preferable.
 * <p>请注意，保存点只能在活动事务中工作。 只需使用此编程式保存点处理即可满足高级需求； 否则，最好使用 PROPAGATION_NESTED 的子事务。
 *
 * <p>This interface is inspired by JDBC 3.0's Savepoint mechanism
 * but is independent from any specific persistence technology.
 * <p>该接口受JDBC 3.0的 Savepoint 机制的启发，但独立于任何特定的持久性技术。
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see TransactionStatus
 * @see TransactionDefinition#PROPAGATION_NESTED
 * @see java.sql.Savepoint
 */
public interface SavepointManager {

	/**
	 * Create a new savepoint. You can roll back to a specific savepoint
	 * via {@code rollbackToSavepoint}, and explicitly release a savepoint
	 * that you don't need anymore via {@code releaseSavepoint}.
	 * <p>创建一个新的事务保存点. 可以通过 {@code rollbackToSavepoint} 回滚到特定的保存点，并通过 {@code releaseSavepoint} 显式释放不再需要的保存点。
	 *
	 * <p>Note that most transaction managers will automatically release
	 * savepoints at transaction completion.
	 * <p> 注意大多数事务管理器将在事务结束时自动释放事务保存点
	 *
	 * @return a savepoint object, to be passed into
	 * {@link #rollbackToSavepoint} or {@link #releaseSavepoint}
	 * @throws NestedTransactionNotSupportedException if the underlying
	 * transaction does not support savepoints
	 * @throws TransactionException if the savepoint could not be created,
	 * for example because the transaction is not in an appropriate state
	 * @see java.sql.Connection#setSavepoint
	 */
	Object createSavepoint() throws TransactionException;

	/**
	 * Roll back to the given savepoint.
	 * <p>回退到给定的事务保存点
	 *
	 * <p>The savepoint will <i>not</i> be automatically released afterwards.
	 * You may explicitly call {@link #releaseSavepoint(Object)} or rely on
	 * automatic release on transaction completion.
	 * <p>之后，保存点将不会自动释放。 您可以显式调用 {@link #releaseSavepoint(Object)} 或在事务完成时依赖自动释放特性。
	 * @param savepoint the savepoint to roll back to
	 * @throws NestedTransactionNotSupportedException if the underlying
	 * transaction does not support savepoints
	 * @throws TransactionException if the rollback failed
	 * @see java.sql.Connection#rollback(java.sql.Savepoint)
	 */
	void rollbackToSavepoint(Object savepoint) throws TransactionException;

	/**
	 * Explicitly release the given savepoint.
	 * <p>显示释放给定的事务
	 *
	 * <p>Note that most transaction managers will automatically release
	 * savepoints on transaction completion.
	 * <p>注意大多数事务管理器将在事务结束时自动释放事务保存点
	 *
	 * <p>Implementations should fail as silently as possible if proper
	 * resource cleanup will eventually happen at transaction completion.
	 * <p>如果在事务完成时最终将进行适当的资源清理，则实现应尽可能安静地失败。
	 *
	 * @param savepoint the savepoint to release
	 * @throws NestedTransactionNotSupportedException if the underlying
	 * transaction does not support savepoints
	 * @throws TransactionException if the release failed
	 * @see java.sql.Connection#releaseSavepoint
	 */
	void releaseSavepoint(Object savepoint) throws TransactionException;

}
