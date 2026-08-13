function isVerified(value) {
  return Number(value) === 1;
}

export function certificationState(profile) {
  const realVerified = isVerified(profile?.realVerified);
  return {
    realVerified,
    marriageVerified: isVerified(profile?.singleVerified),
    realAction: realVerified ? '查看信息' : '去认证',
    marriageAction: realVerified
      ? isVerified(profile?.singleVerified)
        ? '查看结果'
        : '去认证'
      : '需先实名',
  };
}
