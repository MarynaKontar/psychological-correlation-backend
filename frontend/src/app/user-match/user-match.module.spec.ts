import { UserMatchModule } from './user-match.module';

describe('UserMatchModule', () => {
  let userMatchModule: UserMatchModule;

  beforeEach(() => {
    userMatchModule = new UserMatchModule();
  });

  it('should create an instance', () => {
    expect(userMatchModule).toBeTruthy();
  });
});
