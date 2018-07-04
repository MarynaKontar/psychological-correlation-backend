import { UserAnswersModule } from './user-answers.module';

describe('UserAnswersModule', () => {
  let userAnswersModule: UserAnswersModule;

  beforeEach(() => {
    userAnswersModule = new UserAnswersModule();
  });

  it('should create an instance', () => {
    expect(userAnswersModule).toBeTruthy();
  });
});
