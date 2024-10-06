package springbatch_example;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
//import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
//import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.FieldExtractor;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.autoconfigure.security.SecurityProperties.User;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import springbatch.model.User;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {
	
	@Autowired
	public JobBuilderFactory jobBuilderFactory;
	
	@Autowired
	public StepBuilderFactory stepBuilderFactory;
	
	/*
	 * @Autowired public DataSource dataSource;
	 */
	
	
	/*
	 * public DataSource dataSource() { final DriverManagerDataSource dataSource =
	 * new DriverManagerDataSource();
	 * dataSource.setDriverClassName("com.mysql.jdbc.Driver");
	 * dataSource.setUrl("jdbc:mysql://localhost:3306/vivekdb");
	 * dataSource.setUsername("root"); dataSource.setPassword("India@1234567");
	 * 
	 * return dataSource;
	 * 
	 * }
	 */
	
	@Bean
	public FlatFileItemReader<User> reader(){
		FlatFileItemReader<User> reader = new FlatFileItemReader<>();
		reader.setResource(new ClassPathResource("users.csv"));
		reader.setLineMapper(new DefaultLineMapper<>() {{
			setLineTokenizer(new DelimitedLineTokenizer() {{
				setNames(new String[] {"name"});				
			}});
			setFieldSetMapper(new BeanWrapperFieldSetMapper<User>(){{ 
				setTargetType(User.class);				
			}});			
		}});		
		return reader;		
	}
	
	@Bean
	public UserItemProcessor processor() {
		return new UserItemProcessor();		
	}
	
	/*
	 * @Bean public JdbcBatchItemWriter<User> writer(){ JdbcBatchItemWriter<User>
	 * writer = new JdbcBatchItemWriter<>();
	 * writer.setItemSqlParameterSourceProvider(new
	 * BeanPropertyItemSqlParameterSourceProvider<User>());
	 * writer.setSql("INSERT INTO user (name) Values (:name)");
	 * writer.setDataSource(dataSource);
	 * 
	 * return writer; }
	 */
	
	@Bean
	public FlatFileItemWriter<User> writer(){
		FlatFileItemWriter<User> writer = new FlatFileItemWriter<>();
		writer.setResource(new FileSystemResource("output/users_output.csv"));
		writer.setLineAggregator(new DelimitedLineAggregator<User>() {{
			setDelimiter(",");
			setFieldExtractor(new FieldExtractor<User>() {
				
				@Override
				public Object[] extract(User user) {
					return new Object[]{user.getName()};
				}
			});
		}});
		return writer;
	}
	
	@Bean
	public Step step1() {
		return stepBuilderFactory.get("step1").<User, User> chunk(3)
				.reader(reader())
				.processor(processor())
				.writer(writer())
				.build();
		
	}
	
	@Bean
	public Job importUserJob() {
		return jobBuilderFactory.get("importUserJob")
				.incrementer(new RunIdIncrementer())
				.flow(step1())
				.end()
				.build();
	}

}
