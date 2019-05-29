package io.metadew.iesi.server.rest.pagination;

import static java.util.stream.Collectors.toList;

import java.util.List;

import org.springframework.stereotype.Repository;

import io.metadew.iesi.server.rest.controller.JsonTransformation.ConnectionGlobal;

@Repository
public class ConnectionRepository {

	public List<ConnectionGlobal> search(List<ConnectionGlobal> connection, ConnectionCriteria connectionCriteria) {
		if (connectionCriteria.getName() != null) {

			return connection.stream().filter(p -> p.getName().contains(connectionCriteria.getName()))
					.skip(connectionCriteria.getSkip()).limit(connectionCriteria.getLimit()).collect(toList());

		} else if (connectionCriteria.getDescription() != null) {
			return connection.stream().filter(p -> p.getDescription().contains(connectionCriteria.getDescription()))
					.skip(connectionCriteria.getSkip()).limit(connectionCriteria.getLimit()).collect(toList());

		} else if (connectionCriteria.getType() != null) {
			return connection.stream().filter(p -> p.getType().contains(connectionCriteria.getType()))
					.skip(connectionCriteria.getSkip()).limit(connectionCriteria.getLimit()).collect(toList());
		}

		return connection.stream().skip(connectionCriteria.getSkip()).limit(connectionCriteria.getLimit())
				.collect(toList());

	}
}